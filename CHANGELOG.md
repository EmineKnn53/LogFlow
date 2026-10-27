# LogFlow - (CHANGELOG)

Bu dosya, Yazılım Mimarisi dersi kapsamında artımlı olarak geliştirilen LogFlow projesinde sürümlere göre yapılan değişiklikleri, testleri ve mimari kararları belgeler. Sürümler en yeniden eskiye sıralanmıştır.

---

## [v4] - Artım 4: Yapılandırma Odaklı Birleştirme ve Aşama Kaydı

### Eklenen özellikler

- YAML dosyalarından pipeline yapılandırması okuma desteği eklendi (`YamlConfigLoader`).
- `StageFactory` ve `StageRegistry` ile aşama adlarının fabrika işlevlerine eşlenmesi sağlandı.
- `DefaultStageRegistry` içinde `parser`, `filter` ve `projection` aşamaları kaydedildi.
- `PipelineConfigBuilder`, YAML dosyasını okuyarak kaynak, aşamalar ve hedef bileşenlerinden `Pipeline` oluşturacak şekilde geliştirildi.
- Aşama oluşturma işlemindeki sabit `switch` kaldırıldı; `PipelineConfigBuilder`, `StageRegistry.create(...)` kullanacak şekilde güncellendi.
- `Main` için `--config` ile YAML dosyasından çalıştırma desteği eklendi. Önceki komut satırı kullanımı korundu.
- Üç farklı YAML yapılandırması hazırlandı:
  - `configs/error-report.yaml`
  - `configs/api-traffic.yaml`
  - `configs/bandwidth-by-client.yaml`

### Doğrulama ve hata yönetimi

- Desteklenmeyen kaynak/hedef türleri, eksik veya geçersiz yollar ve bilinmeyen aşama türleri için doğrulamalar eklendi.
- Eksik filtre parametresi, yanlış türde `minStatus`, geçersiz projeksiyon alanları ve boş `stages` listesi için hata denetimleri uygulandı.
- Yapılandırma hataları, kayıtlar işlenmeden önce `IllegalArgumentException` ile bildirilir.
- `PipelineConfigBuilderTest` içine yanlış parametre türü ve boş pipeline testleri eklendi.

### Testler ve çalıştırma doğrulaması

- YAML yükleme, kayıt sistemi, yapılandırmadan pipeline oluşturma ve YAML entegrasyon testleri eklendi.
- `error-report.yaml`, `api-traffic.yaml` ve `bandwidth-by-client.yaml` komut satırından başarıyla çalıştırıldı.
- Son JUnit çalıştırması: **50 test bulundu, 50 test başarılı, 0 başarısız** (13 başarılı test kapsayıcısı; 522 ms).
- Bu sonuç mevcut test senaryolarının geçtiğini gösterir; tüm olası çalışma koşullarının doğrulandığı anlamına gelmez.

### Mimari değişiklikler

- Pipeline tanımı Java kodundan YAML yapılandırmasına taşındı.
- Aşama türünün çözümlenmesi, `PipelineConfigBuilder` içindeki sabit koşul dallarından `StageRegistry` kayıt tablosuna devredildi.
- Aynı derlenmiş uygulama, farklı YAML dosyalarıyla farklı işlem hatları çalıştırabilir.
- Mevcut YAML biçimi, `source`, `stages` ve `sink` alanlarını kök düzeyde kullanır; aşama parametreleri aşama nesnesi içindedir. Ders yönergesindeki örnek iç içe `pipeline` / `params` şemasından farklıdır; uygulamanın gerçek biçimi esas alınmalıdır.

### Belgeler ve teslim notu

- `README.md` ve `ARCHITECTURE.md` dört artımı kapsayacak biçimde genişletildi.
- Bu değişiklik günlüğüne Artım 4 eklendi.

---

## [v3] - Artım 3: Filtreleme, Projeksiyon ve PipelineBuilder

### Eklenen özellikler

- `PredicateFilterStage` sınıfı eklendi; `Predicate<LogRecord>` ile koşullu filtreleme sağlandı.
- HTTP durum kodu, URL yolu öneki ve zaman aralığına göre filtreleme desteklendi.
- `ProjectionStage` ile `LogRecord` alanlarının seçilerek aktarılması sağlandı.
- `FileSink` ile dosyaya çıktı yazma eklendi.
- `PipelineBuilder` ile fluent API üzerinden pipeline oluşturma desteklendi.
- `--min-status`, `--path-prefix`, `--fields` ve `--output` komut satırı parametreleri eklendi.
- Filtre sıralaması deneyi gerçekleştirildi ve `ARCHITECTURE.md` dosyasında belgelendi.

### Mimari değişiklikler

- `Pipeline`, birden fazla `Stage` destekleyecek şekilde düzenlendi.
- `Main.java`, `PipelineBuilder` kullanacak şekilde güncellendi.
- Dosyaya yazma için `open()` / `close()` yaşam döngüsü yönetimi kullanıldı.
- Aşamalar fluent API ile birleştirilebilir hale getirildi.

### Eklenen testler

- `PredicateFilterStageTest`: 7 test
- `ProjectionStageTest`: 8 test
- `FileSinkTest`: 7 test
- `PipelineIntegrationTest`: 1 test

Artım 3 kapsamında **23 yeni test** eklendi. Bu artım sonunda kaydedilen JUnit sonucunda **32 test başarılı, 0 başarısız** idi.

### Filtre sıralaması deneyi

200 kayıtlık veri kümesinde iki sıra karşılaştırıldı:

| Ölçüm | Status → Path | Path → Status |
|---|---:|---:|
| Çıktı kaydı | 20 | 20 |
| Status kontrolü | 200 | 100 |
| Path kontrolü | 40 | 200 |
| Toplam kontrol | 240 | 300 |

Her iki sıralama aynı 20 kaydı üretti. Önce status filtresinin kullanılması, bu veri kümesinde toplam filtre kontrol sayısını **%20 azalttı**. Tek çalıştırmadan elde edilen süre farkları kesin performans üstünlüğü olarak yorumlanmadı.

---

## [v2] - Artım 2: LogRecord ve ParserStage

### Eklenen özellikler

- Değişmez `LogRecord` veri modeli Java `record` yapısıyla tanımlandı.
- `ParserStage` ile Common Log Format (CLF) satırları yapılandırılmış kayıtlara dönüştürüldü.
- HTTP metodu, yol, durum kodu ve bayt sayısı gibi alanlar ayrıştırıldı.
- Geçersiz log satırlarını atlama ve sayma özelliği ile `getErrorCount()` metodu eklendi.
- `ConsoleSink`, yapılandırılmış kayıtları okunabilir biçimde gösterecek şekilde güncellendi.

### Testler ve kapsama

- `ParserStage` için **8 birim testi** yazıldı.
- `Pipeline` için **1 test** yazıldı.
- JUnit test altyapısı kullanıldı.
- Önceki Artım 2 kaydında raporlanan JaCoCo **satır kapsama oranı: %76,24**. Bu değer Artım 4 için yeniden ölçülmüş bir oran değildir.

### Mimari kazanımlar

- Ham metin işleme yerine tipli alan modeli kullanıldı.
- Ayrıştırma sorumluluğu ayrı bir aşamaya taşındı.
- Ayrıştırıcı testlerinde dosya sistemi yerine test emitter yaklaşımı kullanılabildi.

---

## [v1] - Artım 1: Temel Pipeline Mimarisi

### Eklenen özellikler

- `Source`, `Stage`, `Sink` ve `Emitter` arayüzleri oluşturuldu.
- `Pipeline` sınıfı geliştirildi.
- `FileLineSource` ile dosyadan satır okuma sağlandı.
- `ConsoleSink` ile konsola çıktı verme sağlandı.
- Temel veri akışı kuruldu:

```text
FileLineSource → Stage → ConsoleSink
```

### Mimari kazanımlar

- Source–Stage–Sink yaklaşımıyla bileşenlerin sorumlulukları ayrıldı.
- İşlem hattı davranışı `Main` sınıfından ayrıştırıldı.
- Sonraki artımlar için genişletilebilir temel oluşturuldu.

---

## Sürüm özeti

| Sürüm | Temel geliştirmeler | Kaydedilen doğrulama |
|---|---|---|
| v1 | Source, Stage, Sink ve temel Pipeline | Temel çalışma akışı |
| v2 | LogRecord, ParserStage, JUnit ve JaCoCo | 8 ayrıştırıcı testi; %76,24 satır kapsama (önceki kayıt) |
| v3 | Filtreleme, projeksiyon, FileSink, PipelineBuilder ve CLI | 32/32 test |
| v4 | YAML, StageFactory, StageRegistry, PipelineConfigBuilder ve üç yapılandırma | 50/50 test; üç YAML komutu başarılı |


---

# Artım 5 — Hata Yönetimi, Doğrulama ve Dead-Letter Kanalı

**Tarih:** 27.10.2026
**Sürüm:** v5
**Durum:** Geliştirme ve testler tamamlandı.

## Eklenen Sınıflar

- PipelineException.java
  - İşlem hattının devam edemediği hataları temsil eder.

- ErrorPolicy.java
  - FAIL_FAST, SKIP, DEAD_LETTER ve RETRY(n)
    hata politikalarını tanımlar.

- FailedRecord.java
  - Hatalı kayıtların ham verisini, hata açıklamasını,
    aşama adını ve zaman bilgisini saklar.

- RunSummary.java
  - Gelen, çıkan, atlanan, filtrelenen ve dead-letter
    kayıt sayılarını ve gerçek çalışma süresini raporlar.

- ValidationStage.java
  - YAML kurallarına göre kayıt doğrulama yapar.

- DirtyFixtureGenerator.java
  - Kaos testi için bozuk ve boş log dosyaları üretir.

- Increment5Test.java
  - Artım 5 için JUnit testlerini içerir.

## Güncellenen Sınıflar

- ParserStage.java
  - Katı hata yönetimi ve StageException desteği eklendi.
  - Önceki artımlarla uyumluluk korundu.

- Pipeline.java
  - Hata politikaları entegre edildi.
  - Bağımsız dead-letter kanalı eklendi.
  - RunSummary desteği eklendi.
  - Kaynak açma ve kapatma işlemleri geliştirildi.

- FileLineSource.java
  - Geçersiz UTF-8 baytlarının işlenmesi geliştirildi.
  - Dosya okuma hataları PipelineException ile yönetildi.

- DefaultStageRegistry.java
  - ValidationStage kayıt mekanizmasına eklendi.
  - ParserStage katı hata modunda kullanılabilir hale geldi.

- PipelineConfigBuilder.java
  - YAML hata politikalarının okunması eklendi.
  - Dead-letter hedefi yapılandırması eklendi.
  - Eksik ve geçersiz yapılandırma kontrolleri genişletildi.

## Eklenen Yapılandırma Dosyaları

- configs/dirty-validation.yaml
- configs/fail-fast-test.yaml
- configs/skip-test.yaml
- configs/retry-test.yaml
- configs/empty-test.yaml

## Eklenen Test Verileri

- data/access-dirty.log
- data/access-empty.log

Kaos dosyasında kesilmiş satır, geçersiz UTF-8 baytı,
negatif byte, 1970 ve 2099 tarihleri, uzun satır ve
geçersiz IP adresi senaryoları bulunmaktadır.

## Test Sonuçları

| Test | Sonuç |
|---|---|
| DEAD_LETTER | Başarılı |
| FAIL_FAST | Başarılı |
| SKIP | Başarılı |
| RETRY(2) hata sonlandırma | Başarılı |
| RETRY(2) başarılı yeniden deneme | Başarılı |
| Boş dosya | Başarılı |
| Eksik dead-letter hedefi | Başarılı |
| Kaos testi | Başarılı |

## JUnit Sonucu

- Toplam test: 56
- Başarılı test: 56
- Başarısız test: 0

## Kaos Testi Sonucu

- Gelen kayıt: 9
- Geçerli kayıt: 2
- Dead-letter kayıt: 7
- Kayıp kayıt: 0

## Mimari Kazanımlar

- Kayıt düzeyindeki hatalar sistem hatalarından ayrıldı.
- Hata politikaları yapılandırılabilir hale getirildi.
- Ana veri akışından bağımsız hata kanalı oluşturuldu.
- Doğrulama kuralları bildirimsel YAML yapısına taşındı.
- Performans ölçümleri için RunSummary altyapısı eklendi.
```

## 3. Kaydet

`Ctrl + S` ile kaydet.

### Şu ana kadarki durum

| Teslim gereksinimi | Durum |
|---|---|
| Hata politikaları | <badge color="success">Tamamlandı</badge> |
| ValidationStage | <badge color="success">Tamamlandı</badge> |
| Dead-letter kanalı | <badge color="success">Tamamlandı</badge> |
| RunSummary | <badge color="success">Tamamlandı</badge> |
| Kaos testi | <badge color="success">Tamamlandı</badge> |
| 56 JUnit testi | <badge color="success">Başarılı</badge> |
| ARCHITECTURE.md | <badge color="success">Hazır</badge> |
| README.md | <badge color="success">Hazır</badge> |
| CHANGELOG.md | <badge color="info">Bu adım</badge> |
| Git v5 etiketi | <badge>Bekliyor</badge> |

**Emine, dosyayı kaydedince “Adım 25 tamam” yaz.**

Sonraki adımda önce `git status` ile değişiklikleri kontrol edeceğiz. Ardından Artım 5'i ayrı bir commit olarak kaydedip hocanın istediği `v5` etiketini oluşturacağız.