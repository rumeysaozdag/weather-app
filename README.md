# Havadaş

**Bugün gökyüzünde ne var?**

Havadaş, şehirlerin güncel hava durumunu ve tahminlerini gösteren bir Android uygulamasıdır. Şehir arayabilir, bir şehri favorilerine ekleyebilir ve yağış olasılığına göre önümüzdeki saatler için yol önerisi alabilirsin. Uygulama içi hava asistanı da seçili şehrin tahminine göre sıcaklık, yağış, şemsiye ve kıyafet sorularına yanıt verir.

Uygulama Kotlin ve Jetpack Compose ile geliştirilmiştir. Hava ve şehir verileri, API anahtarı gerektirmeyen Open-Meteo servislerinden alınır.

## Eğitim kaynağı ve proje ilişkisi

Havadaş, [Bilişim Kampı Android eğitimindeki hava durumu referans projesinden](https://github.com/tc-yusufburakdemir/weather-app) yola çıkar. Kaynak depoda şehir arama, tahmin ekranı ve üç katmanlı mimari adım adım gösterilir. Bu depoda ise bu temel; favori şehirler, yol önerisi, uygulama içi hava asistanı ve Havadaş arayüzüyle genişletilmiştir. Kaynak projedeki mimari notları için [ARCHITECTURE.md](https://github.com/tc-yusufburakdemir/weather-app/blob/main/docs/ARCHITECTURE.md) dosyasına bakabilirsin.

## Uygulamaya bakış

![Havadaş uygulamasının şehir arama, hava tahmini ve yol önerisi ekranlarının arayüz önizlemesi](docs/images/app-overview.svg)

*Bu görsel uygulama arayüzünün önizlemesidir; içindeki hava durumu değerleri örnektir, canlı ekran görüntüsü değildir.*

## Özellikler

- Şehir adıyla arama ve arama sonucundan şehir seçme
- Anlık sıcaklık, hissedilen sıcaklık, nem, rüzgâr ve hava durumu bilgisi
- Saatlik ve 7 günlük hava tahmini
- Şehirleri favorilere kaydetme; kayıtlar cihazda saklanır
- Favori şehirler için önümüzdeki saatlerin yağış olasılığına dayalı yol önerisi
- Hava Asistanı ile hava tahminine dayalı sorulara uygulama içi yanıtlar
- Yükleniyor, sonuç yok, veri ve hata durumları için ayrı ekranlar

> Yol önerileri hava tahminine dayanır; uygulama canlı trafik veya harita rotası sağlamaz. Hava Asistanı uygulama içinde tahmin verisini kurallarla yorumlar; çevrimiçi bir yapay zekâ servisine bağlanmaz.

## Teknolojiler

- Kotlin 2.2.20 ve Java 17
- Jetpack Compose, Material 3 ve Navigation Compose
- ViewModel, StateFlow ve Kotlin Coroutines
- Retrofit, Gson ve OkHttp
- Open-Meteo Geocoding API ve Forecast API
- JUnit ve MockWebServer ile birim testleri

## Nasıl çalışır?

Arayüz, veri alma ve ekran durumu birbirinden ayrıdır:

```text
Compose ekranı → ViewModel (StateFlow) → Repository → Retrofit → Open-Meteo
```

`ui/` kullanıcı arayüzünü, `model/` şehir ve tahmin modellerini, `data/` ise API bağlantısını ve favori şehirlerin yerel kaydını içerir. ViewModel'ler yükleniyor, başarılı, boş ve hata durumlarını üretir; Compose ekranları bu duruma göre görünümü günceller.

## Open-Meteo API ve veri dönüşümü

Şehir arama ve hava tahmini için Open-Meteo'nun iki servisi kullanılır; API anahtarı gerekmez.

| Amaç | İstek örneği |
|---|---|
| Şehir arama | `https://geocoding-api.open-meteo.com/v1/search?name=Ankara&count=10&language=tr` |
| Hava tahmini | `https://api.open-meteo.com/v1/forecast?latitude=39.92&longitude=32.85&current=...&hourly=...&daily=...&forecast_days=7&timezone=auto` |

Tahmin servisi saatlik ve günlük alanları ayrı listeler halinde, yani sütun biçiminde döndürür. Aynı listedeki aynı indeks birbiriyle eşleşen zaman, sıcaklık ve hava durumu değerlerini verir; `ForecastResponse.toForecast()` bu veriyi ekranların kullandığı tahmin modellerine çevirir. Open-Meteo'nun WMO `weather_code` değeri `WeatherCondition.fromWeatherCode()` ile Türkçe açıklama ve simgeye eşlenir. Şehir aramasında `results` dönmezse ekran boş sonuç durumunu gösterir.

## Gereksinimler ve çalıştırma

- Android Studio
- JDK 17
- Android SDK 36
- Android 8.0 (API 26) veya üzeri bir cihaz/emülatör

Projeyi Android Studio'da açıp Gradle eşitlemesinin tamamlanmasını bekleyebilirsin. Komut satırından Windows'ta:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

macOS veya Linux'ta:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Debug APK, `app/build/outputs/apk/debug/` altında oluşturulur. Android SDK yolu bulunamazsa `local.properties` dosyasındaki `sdk.dir` değerini veya `ANDROID_HOME` ortam değişkenini ayarla. `local.properties` kişisel makine ayarıdır ve depoya eklenmez.

## Sürüm bilgisi

| Alan | Değer |
|---|---|
| Uygulama sürümü | 1.0.0 |
| Android sürüm kodu | 2 |
| Minimum Android | API 26 (Android 8.0) |
| Hedef Android API | 36 |

Sürüm değerleri `app/build.gradle.kts` içinde tanımlıdır.

## Proje yapısı

```text
app/src/main/java/com/kampplus/hava/
├── data/       Open-Meteo istemcileri, repository'ler ve favori kaydı
├── model/      Şehir, tahmin ve hava durumu modelleri
└── ui/
    ├── city/      Şehir arama ve favoriler
    ├── forecast/  Hava durumu ve tahmin ekranı
    ├── route/     Yağışa dayalı yol önerisi
    ├── component/ Paylaşılan bileşenler ve Hava Asistanı
    └── theme/     Compose tema ve tipografi
```

## Eğitim checkpoint'leri

Kaynak eğitim deposu uygulamayı ders aşamalarına ayırır. Bu branch'ler **kaynak depoda** bulunur; burada listelenmeleri Havadaş deposunda aynı branch'lerin bulunduğu anlamına gelmez.

| Kaynak branch | Aşama | İçerik |
|---|---|---|
| [`cp1-baslangic`](https://github.com/tc-yusufburakdemir/weather-app/tree/cp1-baslangic) | Başlangıç | Boş Empty Activity ve “Merhaba Kamp!” |
| [`cp1-bitis` / `cp2-baslangic`](https://github.com/tc-yusufburakdemir/weather-app/tree/cp1-bitis) | Uygulama 2 | Sabit şehir listesi, `CityCard`, plaka rozeti, `LazyColumn` ve karta dokununca `Log.d` |
| [`cp2-bitis` / `cp3-baslangic`](https://github.com/tc-yusufburakdemir/weather-app/tree/cp2-bitis) | Uygulama 3 | Arama, `rememberSaveable`, filtreleme, boş durum, `CityViewModel` ve `model/data/ui` paketleri |
| [`cp3-bitis` / `cp4-baslangic`](https://github.com/tc-yusufburakdemir/weather-app/tree/cp3-bitis) | Uygulama 4 | Retrofit + Gson, `ApiClient`, `CityRepository`, dört yükleme durumu, Shimmer ve `try/catch` |
| [`cp4-bitis` (kaynak depoda `main`)](https://github.com/tc-yusufburakdemir/weather-app/tree/cp4-bitis) | Uygulama 5 | Tahmin ekranı, navigasyon, arayüz cilası ve testler |

Kaynak depodaki [`archive/v1-clean-arch`](https://github.com/tc-yusufburakdemir/weather-app/tree/archive/v1-clean-arch) branch'i önceki Clean Architecture + Hilt + Room sürümünü içerir.

## Testler

`app/src/test/` altındaki birim testleri model, API ve ekran durumlarını korur:

| Test | Kapsadığı davranış |
|---|---|
| `WeatherConditionTest` | WMO kodlarının Türkçe hava durumu ve simgelere eşlenmesi |
| `CityApiTest` | Şehir yanıtı alanlarının Gson ile doğru okunması |
| `ForecastApiTest` | Tahmin yanıtındaki sütunların saatlik/günlük modellere dönüştürülmesi |
| `CityViewModelTest` | Arama, Loading/Success/Empty/Error durumları ve tekrar deneme |
| `ForecastViewModelTest` | Navigasyon parametreleri, tahmin yükleme ve hata durumları |

## Release imzası

Release derlemesini kendi anahtarınla imzalamak için `keystore.properties.example` dosyasını `keystore.properties` adıyla kopyalayıp kendi imza bilgilerinle doldur. Gerçek imza dosyaları ve anahtarlar depoya eklenmemelidir. Bu dosya yoksa release derlemesi debug anahtarını kullanır; mağazada yayımlamak için kendi release anahtarın gerekir.

Release APK oluşturmak için Windows'ta `.\gradlew.bat assembleRelease`, macOS/Linux'ta `./gradlew assembleRelease` komutunu kullanabilirsin.
