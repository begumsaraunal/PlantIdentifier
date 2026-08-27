# Plant AI proxy

Android uygulaması PlantNet anahtarını taşımaz. Proxy anahtarı yalnızca sunucu ortam değişkeninden alır ve Android’den gelen multipart isteğini PlantNet’e iletir.

> Güvenlik: API anahtarı `local.properties`, APK, Git, `.env` dosyası veya loglara eklenmemelidir. Daha önce bir anahtar paylaşıldıysa PlantNet panelinden iptal edilip yenisi oluşturulmalıdır.

## Yerelde çalıştırma (PowerShell)

```powershell
$env:PLANTNET_API_KEY = "PlantNet-anahtarınız"
$env:PLANT_PROXY_TOKEN = "Uygulamanızın-proxy-tokeni"
cd server
npm start
```

`PLANT_PROXY_TOKEN` tanımlanırsa Android istekleri için aynı token’ı `PLANT_PROXY_TOKEN` Gradle property’siyle yapılandırın. Production ortamında token, HTTPS ve reverse proxy arkasında kullanılmalıdır. Token tanımlı değilse yerel geliştirme kolaylığı için kimlik doğrulama kapalıdır.

Sağlık kontrolü: `http://localhost:8787/healthz`

Android emulator için `local.properties` içine şunu ekleyin:

```properties
PLANT_API_BASE_URL=http://10.0.2.2:8787/
```

Gerçek cihazda `10.0.2.2` yerine bilgisayarın aynı ağdaki IP adresini kullanın. Üretimde HTTPS kullanan bir domain’e deploy edin ve Android’de yalnızca o URL’yi tanımlayın. `PLANTNET_API_KEY` kesinlikle APK, Git veya loglara konulmamalıdır.
