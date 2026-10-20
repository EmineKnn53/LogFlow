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
