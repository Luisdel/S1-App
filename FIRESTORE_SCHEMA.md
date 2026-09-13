# Esquema de Base de Datos Cloud Firestore & Sincronización Diferencial

## 1. Arquitectura Multi-Tenant y Topología de Colecciones

Para garantizar el **aislamiento físico**, **opacidad total entre organizaciones** y optimización del **Plan Spark gratuito** (evitando lecturas globales innecesarias), se adopta una topología de **subcolecciones jerárquicas** anidadas bajo el código de empresa:

```text
companies/{companyCode}
  ├── employees/{employeeId}
  ├── shifts/{shiftId}
  ├── time_clocks/{clockId}
  └── time_off_requests/{requestId}
```

---

## 2. Definición Detallada de Colecciones y Campos

### 🏢 Colección: `companies`
- **Ruta**: `/companies/{companyCode}` (ej: `/companies/S1-CORP`)
- **Descripción**: Datos del entorno empresarial y credenciales del Administrador Maestro.

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :---: | :--- |
| `code` | `string` | Sí | Código alfanumérico único en mayúsculas (ej: `"S1-CORP"`). Clave del documento. |
| `name` | `string` | Sí | Razón social o nombre comercial de la empresa. |
| `adminEmail` | `string` | Sí | Correo electrónico corporativo o civil del Administrador Maestro. |
| `adminName` | `string` | Sí | Nombre y apellidos del administrador principal. |
| `createdAt` | `number` (Epoch ms) / `timestamp` | Sí | Marca de tiempo de alta de la organización. |
| `updatedAt` | `number` (Epoch ms) / `timestamp` | Sí | **Campo de sincronización diferencial**. |

---

### 👥 Subcolección: `employees`
- **Ruta**: `/companies/{companyCode}/employees/{employeeId}`
- **Descripción**: Perfiles de empleados dados de alta previamente por el Administrador.

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :---: | :--- |
| `id` | `number` / `string` | Sí | Identificador interno único del empleado (o UID de Firebase Auth). |
| `companyCode` | `string` | Sí | Código de la empresa a la que pertenece. |
| `name` | `string` | Sí | Nombre completo del empleado. |
| `email` | `string` | Sí | Correo corporativo o civil asignado para iniciar sesión. |
| `phone` | `string` | No | Número de teléfono de contacto. |
| `passwordHash` | `string` | Sí | Clave de acceso asignada por el Administrador. |
| `isMasterAdmin`| `boolean` | Sí | `true` si es el Administrador Maestro (cuenta protegida e inmutable). |
| `authProvider` | `string` | Sí | `"LOCAL"` o `"GOOGLE"`. |
| `jobTitle` | `string` | Sí | Puesto de trabajo (ej: `"Operador Logístico"`, `"Supervisor"`). |
| `department` | `string` | Sí | Departamento asignado (ej: `"Operaciones"`, `"Logística"`). |
| `project` | `string` | No | Proyecto o centro de trabajo asignado. |
| `functionalArea`| `string` | No | Área funcional interna. |
| `systemRole` | `string` | Sí | `"ADMIN"`, `"MANAGER"` o `"EMPLOYEE"`. |
| `workSchedulePattern` | `string` | Sí | `"LUNES_A_VIERNES"`, `"SABADO_DOMINGO_LUNES"`, `"JUEVES_A_DOMINGO"`, `"ROTATIVO_TOTAL"`. |
| `status` | `string` | Sí | `"ACTIVO"`, `"VACACIONES"`, `"BAJA_MEDICA"`, `"INACTIVO"`. |
| `avatarColorHex` | `number` | Sí | Color asignado para la insignia/avatar en la UI. |
| `hireDate` | `string` | Sí | Fecha de contratación en formato `"YYYY-MM-DD"`. |
| `notes` | `string` | No | Observaciones internas o notas de RRHH. |
| `updatedAt` | `number` (Epoch ms) / `timestamp` | Sí | **Campo de sincronización diferencial**. |

---

### ⏱️ Subcolección: `shifts`
- **Ruta**: `/companies/{companyCode}/shifts/{shiftId}`
- **Descripción**: Turnos programados y cuadrantes de trabajo generados por supervisores o administradores.

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :---: | :--- |
| `id` | `number` / `string` | Sí | Identificador único del turno. |
| `companyCode` | `string` | Sí | Código de la empresa. |
| `employeeId` | `number` / `string` | Sí | ID del empleado asignado. |
| `employeeName` | `string` | Sí | Nombre del empleado al momento de la asignación. |
| `department` | `string` | Sí | Departamento o sección del turno. |
| `date` | `string` | Sí | Fecha del turno en formato `"YYYY-MM-DD"`. |
| `shiftType` | `string` | Sí | `"MANANA"`, `"TARDE"`, `"NOCHE"`, `"PARTIDO"`, `"GUARDIA"`. |
| `startTime` | `string` | Sí | Hora de inicio en formato `"HH:mm"`. |
| `endTime` | `string` | Sí | Hora de finalización en formato `"HH:mm"`. |
| `notes` | `string` | No | Instrucciones especiales para el turno. |
| `updatedAt` | `number` (Epoch ms) / `timestamp` | Sí | **Campo de sincronización diferencial**. |

---

## 3. Estrategia de Sincronización Diferencial (Delta Sync)

Para garantizar un rendimiento de alta velocidad y respetar los límites gratuitos del **Plan Spark** (50.000 lecturas / 20.000 escrituras al día):

1. **Persistencia del Cursor Local**: El cliente almacena el valor `lastSyncTimestamp` (obtenido de `System.currentTimeMillis()`) en `SharedPreferences`.
2. **Consulta Diferencial en Firestore**:
   ```kotlin
   // Solo descarga documentos modificados desde la última sincronización
   firestore.collection("companies").document(companyCode)
       .collection("shifts")
       .whereGreaterThan("updatedAt", lastSyncTimestamp)
       .get()
   ```
3. **Escrituras en Lotes (Batches)**: Las modificaciones o fichajes locales pendientes de sincronizar se envían mediante `WriteBatch` (máximo 500 operaciones por lote atómico), evitando llamadas de red individuales.

---

## 4. Índices Compuestos Requeridos en Firestore

Para ejecutar las consultas diferenciales sin errores:

| Colección | Campos Indexados | Orden | Propósito |
| :--- | :--- | :--- | :--- |
| `companies/{companyCode}/employees` | `companyCode` (Asc) + `updatedAt` (Asc) | Ascendente | Sincronización delta de empleados |
| `companies/{companyCode}/shifts` | `companyCode` (Asc) + `updatedAt` (Asc) | Ascendente | Sincronización delta de turnos |
| `companies/{companyCode}/shifts` | `employeeId` (Asc) + `date` (Asc) | Ascendente | Consulta rápida de cuadrante por empleado |
| `companies/{companyCode}/time_clocks` | `employeeId` (Asc) + `timestamp` (Desc) | Mixto | Historial de fichajes de personal |
