# Harita verisi ve yönlendirme

Yol ekranı, OpenStreetMap harita karolarını `osmdroid` ile yükler. Harita gösterilirken
`© OpenStreetMap contributors` atfı ekranda görünür. OpenStreetMap verisi ODbL
lisansına tabidir; karo sunucusunun kullanım politikası ayrıca geçerlidir:
[OpenStreetMap Copyright](https://www.openstreetmap.org/copyright) ve
[Tile Usage Policy](https://operations.osmfoundation.org/policies/tiles/).

Rota çizgisi ve mesafe/süre, prototipte OSRM demo yönlendirme servisiyle alınır.
Bu paylaşımlı demo servisi üretim hizmet seviyesi sunmaz; üretim yayını öncesinde
uygun bir yönlendirme sağlayıcısı veya kendi barındırılan OSRM servisi kullanılmalıdır.
Servisin yanıtladığı süre, canlı trafik yoğunluğunu içermez.

Hava Asistanı ve hava tahminindeki yakın yol özeti, TomTom Traffic Flow Segment
API'sinden en yakın tek yol kesitinin hız bilgisini alabilir. Bunun için
`local.properties` içindeki `TOMTOM_API_KEY` gerekir. Anahtar Android APK'sında
çıkarılabileceğinden, yayın sürümünde kısıtlama veya sunucu tarafı proxy kullanılmalıdır.

Harita ekranındaki yağış radarı, RainViewer Weather Maps API'nin geçmiş radar
karolarını kullanır. API anahtarı istemez; zaman çizgisinde son radar kareleri
oynatılabilir. Sağlayıcı radar verisini 10 dakikalık adımlarla yayımlar ve
kişisel/eğitim amaçlı kullanıma sunar; kullanılabilirlik garantisi vermez.
Ekranda **Weather data by RainViewer** atfı görünür. Ayrıntılar:
[RainViewer API koşulları](https://www.rainviewer.com/api.html) ve
[Weather Maps API](https://www.rainviewer.com/api/weather-maps-api.html).

`app/src/main/assets/turkiye_iller.geojson` dosyası, haritaya dokunulan konumun il
adını bulup işaretçi/rota özetinde göstermek için kullanılır.
