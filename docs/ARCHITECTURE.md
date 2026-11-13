# Mimari

Tek `:app` modülü, üç katman: `model/` · `data/` · `ui/`.

```text
UI (Composable)  →  ViewModel (StateFlow)  →  Repository (suspend)  →  Retrofit → Open-Meteo / RainViewer / TomTom / OSRM
```

Kurallar:

- **UI internete gitmez.** Composable yalnızca aldığı state'i çizer ve olayları yukarı bildirir.
- **ViewModel çizim yapmaz.** Renk veya Composable bilmez; yalnızca state üretir.
- **Repository ekranı bilmez.** Veriyi getirir; nasıl gösterileceği onu ilgilendirmez.

## Neden bu kadar sade?

Proje bilerek okunabilir ve modüler tutuldu. Bağımlılık enjeksiyonu (Hilt), UseCase katmanı veya karmaşık DTO zincirleri olmadan doğrudan üç katmanlı veri akışı uygulanmıştır.

`LoadState<out T>` jenerik tutuldu: tüm ekranlar (`Loading`, `Success`, `Empty`, `Error`) aynı dört durumu paylaşır.

## Veri akışı

```text
Retrofit → Repository (suspend, withContext(IO)) → ViewModel (try/catch → LoadState)
         → Route (collectAsStateWithLifecycle) → Screen (stateless) → Composable
```

1. Kullanıcı arama kutusuna yazar → `onQueryChange` → `_query.value` güncellenir ve 350 ms debounce sonrasında arama tetiklenir.
2. `_uiState.value = LoadState.Loading` → ekranda `ShimmerList` görünür.
3. `repository.search(query)` askıya alınır; ana thread serbest kalır.
4. Cevap gelir → liste boşsa `Empty`, doluysa `Success`; hata olursa `catch` → `Error`.
5. `collectAsStateWithLifecycle` yeni değeri okur, Compose ekranı yeniden çizer.

## Hata yönetimi

Hata **ViewModel'de** yakalanır ve orada insan diline çevrilir.
Repository hatayı sarmalamaz, olduğu gibi fırlatır.

| Ne oldu | Kotlin'de | Kullanıcıya |
|---|---|---|
| İnternet yok, zaman aşımı | `IOException` | "İnternet bağlantını kontrol et." |
| Sunucu 4xx/5xx döndü | `retrofit2.HttpException` | "Sunucu şu an cevap vermiyor." |
| Beklenmeyen JSON | `JsonSyntaxException` | "Bir şeyler ters gitti. Tekrar dene." |
| Eksik `current` bloğu | `IllegalStateException` | "Bir şeyler ters gitti. Tekrar dene." |

Teknik ayrıntı her zaman `Log.e(TAG, ..., e)` ile Logcat'e yazılır.

## Dört durum

`LoadState` `sealed` olduğu için `when` dallarından biri unutulursa derleme hatası alınır.
Her ekran dört dalın tamamını karşılar:

| Durum | Liste ekranı | Tahmin ekranı |
|---|---|---|
| `Loading` | `ShimmerList` | `CircularProgressIndicator` |
| `Success` | `LazyColumn` + `CityCard` | Başlık + saatlik + tıklanabilir 7 günlük + atmosfer panosu |
| `Empty` | "Şehir bulunamadı" + öneri | "Bu şehir için tahmin bulunamadı." |
| `Error` | Mesaj + "Tekrar dene" | Mesaj + "Tekrar dene" |

## Route / Screen ayrımı

Her ekran iki parçadır:

- **`XRoute`** — ViewModel'i edinir, `collectAsStateWithLifecycle` ile state'i toplar.
- **`XScreen`** — ViewModel tanımaz; aldığı değerleri çizer, olayları yukarı bildirir.

Bu ayrım sayesinde ekranlar `@Preview` içinde ve testte elle state verilerek çalıştırılabilir (state hoisting — "değer aşağı, olay yukarı").

## Navigasyon

Alt gezinme çubuğunda (`BottomNavigationBar`) dört ana rota ve detay rotası yer alır:

- `city`: Şehir arama, mevcut GPS konumu ve kayıtlı şehirler
- `turkey-map`: Türkiye haritası, OSRM rota çizimi ve RainViewer canlı yağış radarı
- `route-advice`: Önümüzdeki saatlerin yağış olasılığına göre yol önerisi
- `favorites`: Kayıtlı favori şehirler listesi
- `forecast?name=..&subtitle=..&latitude=..&longitude=..`: Seçilen şehrin detaylı hava tahmini, tıklanabilir 7 günlük/saatlik tablo ve atmosfer panosu

Parametreler yol parçası yerine sorgu parametresidir; çünkü `subtitle` boş olabilir ve boş bir yol parçası rotayı eşleşmez hâle getirir. Değerler `Uri.encode` ile kaçışlanır. `ForecastViewModel` parametreleri `SavedStateHandle` üzerinden okur.

## Genişletme noktaları

| Senaryo | Eklenir | Değişir | Dokunulmaz |
|---|---|---|---|
| Yeni hava durumu görünümü | `WeatherCondition`'a yeni sabit | `fromWeatherCode` içindeki `when` | Ekranlar, ViewModel'ler |
| Yeni hava değişkeni (ör. UV, AQI, polen) | `CurrentResponse` / `AirQualityResponse` + model alanı | `ForecastApi` / `AirQualityApi`, ekran | Repository, ViewModel |
| Yeni asistan sorusu veya radar analizi | `WeatherAssistant.kt` içinde yeni kural | `answerWeatherQuestion` / `buildRadarAnswer` | Diğer ekranlar |
| Farklı veri kaynağı | Yeni `CityRepository` / `WeatherRepository` implementasyonu | ViewModel'in varsayılan parametresi | Ekranlar, model |
