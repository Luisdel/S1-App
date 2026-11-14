# Contribuir a S1 App

## Antes de empezar

1. Crea una rama descriptiva a partir de `main`.
2. No incluyas `google-services.json`, `.env`, claves de firma ni otros
   secretos.
3. Mantén los cambios pequeños y conserva el comportamiento existente salvo
   que la propuesta indique lo contrario.

## Desarrollo

Ejecuta antes de abrir el pull request:

```bash
./gradlew check lintDebug assembleDebug
```

Si una validación requiere un emulador, indícalo en la descripción. Añade o
actualiza tests cuando el cambio afecte a lógica que pueda probarse de forma
aislada.

## Pull requests

Explica el problema, el cambio realizado y las validaciones ejecutadas. Incluye
capturas para cambios visuales y documenta cualquier limitación del entorno.
Todos los cambios deben pasar la CI y recibir una revisión antes de fusionarse.
