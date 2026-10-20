# LogFlow — Yazılım Mimarisi Dönem Projesi

**Java ile bileşen tabanlı, genişletilebilir log işleme hattı (pipeline)**  
**Kapsam:** Artım 1, Artım 2, Artım 3 ve Artım 4  
**Depo:** https://github.com/EmineKnn53/LogFlow

## 1. Proje tanımı ve amacı

LogFlow, web sunucusu erişim kayıtlarını aşamalı bir işlem hattında okuyup işleyen bir Java konsol uygulamasıdır. Proje, Yazılım Mimarisi dersi kapsamında her artımda önceki mimariyi genişleterek geliştirilmiştir. Temel amaç; **bileşenler ve bağlayıcılar**, **arayüz–gerçekleştirim ayrımı**, **sorumlulukların ayrılması**, **modülerlik**, **bileşilebilirlik** ve **yapılandırma ile gerçekleştirim ayrımı** ilkelerini uygulamaktır.

Uygulama, önce ham log satırlarını konsola aktarabilen bir iskelet olarak başlamış; daha sonra tipli kayıt ayrıştırma, filtreleme, projeksiyon, dosya çıktısı ve YAML üzerinden bildirimsel yapılandırma yetenekleri kazanmıştır.

### Genel veri akışı

```text
FileLineSource (Source<String>)
       |
       v
ParserStage (String -> LogRecord)
       |
       v
PredicateFilterStage (LogRecord -> LogRecord) [isteğe bağlı]
       |
       v
ProjectionStage (LogRecord -> Map<String, Object>) [seçili alanlar]
       |
       v
FileSink (Sink<Map<String, Object>>)
       |
       v
Çıktı dosyası
```

İlk artımda `FileLineSource -> Pipeline -> ConsoleSink` akışı kullanılır. Sonraki artımlarda bileşenler eklenir; yapılandırma dosyasında tanımlanan aşamalar farklı kombinasyonlarla çalıştırılabilir. Aşama sıralaması, giriş/çıkış veri türleriyle uyumlu olmalıdır.

## 2. Kullanılan teknolojiler

| Teknoloji | Kullanım amacı |
| --- | --- |
| Java (JDK 17+) | Uygulama, arayüzler, `record` ve pipeline bileşenleri |
| SnakeYAML 2.7 | YAML yapılandırma dosyalarını okumak |
| JUnit 5 / JUnit Platform Console 6.1.3 | Birim ve entegrasyon testleri |
| Git / GitHub | Sürüm kontrolü ve artım etiketleri |
| PowerShell | Windows üzerinde derleme ve çalıştırma |

> Bu belgedeki komutlar Windows PowerShell içindir ve proje ana klasöründen çalıştırılır.

## 3. Temel mimari ve bileşenler

| Bileşen | Görevi |
| --- | --- |
| `Source<O>` | Veriyi üretip `Emitter` üzerinden hatta aktarır. |
| `FileLineSource` | Metin dosyasını satır satır okur. |
| `Stage<I,O>` | Gelen öğeyi işler; sıfır, bir veya daha fazla öğe üretebilir. |
| `Emitter<T>` | Aşamalar arasında çıktı iletimini sağlar. |
| `Sink<I>` | İşlem hattının sonundaki çıktıyı tüketir. |
| `ConsoleSink` | Çıktıyı konsolda gösterir. |
| `Pipeline` | Kaynak, sıralı aşamalar ve hedefi çalıştırır; yaşam döngüsünü yönetir. |
| `LogRecord` | Ayrıştırılmış erişim kaydının alan modelidir. |
| `ParserStage` | CLF satırlarını `LogRecord` nesnesine dönüştürür. |
| `PredicateFilterStage` | `Predicate<LogRecord>` koşulunu sağlayan kayıtları geçirir. |
| `ProjectionStage` | Kaydın yalnızca seçilen alanlarını üretir. |
| `FileSink` | Çıktıyı dosyaya yazar; `open()` ve `close()` kullanır. |
| `PipelineBuilder` | `from(...).via(...).to(...)` akıcı API'siyle hat kurar. |
| `YamlConfigLoader` | YAML dosyasını yükler ve temel yapısını doğrular. |
| `StageFactory` | Bir aşamayı yapılandırma parametrelerinden üretme sözleşmesidir. |
| `StageRegistry` | Aşama adı ile ilgili fabrikayı eşler. |
| `DefaultStageRegistry` | `parser`, `filter`, `projection` kayıtlarını sağlar. |
| `PipelineConfigBuilder` | YAML'deki source, stages ve sink bilgilerini işleyen pipeline'a dönüştürür. |
| `Main` | CLI argümanlarını yorumlar, uygun kurulum yolunu seçer ve pipeline'ı çalıştırır. |

`Stage<I,O>` öğeyi doğrudan döndürmek yerine `Emitter<O>` üzerinden iletir; bu sayede bir aşama **hiç çıktı üretmeyebilir**, **bir çıktı** veya **birden çok çıktı** üretebilir.

## 4. Artım 1 — İskelet Pipeline

**Mimari odak:** Bileşenler ve bağlayıcılar; arayüz ile gerçekleştirim arasındaki fark.

### Amaç ve uygulama

- `Record`, `Stage`, `Source`, `Sink`, `Pipeline` kavramlarını tanımlamak.
- Küçük ve bağımsız `Stage<I,O>`, `Emitter<T>`, `Source<O>`, `Sink<I>` arayüzlerini oluşturmak.
- `FileLineSource` ile dosyadaki her satırı okumak ve `ConsoleSink` ile göstermek.
- Veri akışını `Main` içine yığmadan `Pipeline.run()` üzerinden gerçekleştirmek.
- Örnek log verisini `data/access-small.log` altında bulundurmak (ödev hedefi yaklaşık 200 satır).

**Beklenen çalışma:** Dosyanın satırları işlem hattı üzerinden konsola aktarılır. `Main` yalnızca argümanları alır, hattı kurar ve çalıştırır.

**Ödev teslimleri:** `v1` Git etiketi; temel arayüzler, `FileLineSource`, `ConsoleSink`, `Pipeline`, `Main`, `README.md`; `ARCHITECTURE.md` içinde iki kutulu ilk mimari diyagram.

> **Sürüm notu:** Güncel `Main` artık Artım 3 ve Artım 4 özelliklerini içerir. Artım 1'in özgün davranışı ve teslim kapsamı, `v1` sürümünden ayrıca doğrulanmalıdır.

## 5. Artım 2 — Tipli Kayıtlar ve İlk Gerçek Filtre

**Mimari odak:** Sorumlulukların ayrılması, veri soyutlama ve test disiplininin başlangıcı.

### Amaç ve uygulama

- `LogRecord` ile ham `String` yerine tipli alan modeli taşımak.
- `ParserStage` ile Common Log Format (CLF) satırlarını ayrıştırmak.
- `ConsoleSink` üzerinden kayıtları okunabilir tek satır biçiminde göstermek.
- Hatalı satırları **atlamak ve saymak**; ileri hata işleme mekanizmasını bu artımda uygulamamak.
- Dosya sistemine başvurmayan `CollectingEmitter` test dublörüyle parser testlerini yürütmek.

`LogRecord` alanları:

```java
Instant timestamp;
String clientIp, method, path;
int status;
long bytes;
String userAgent;
Map<String, String> attributes;
String raw;
```

`attributes`, sonraki artımlarda kayıt biçimini genişletmek için ayrılmıştır. Modelin dışarıdan değiştirilememesi hedeflenir; `Map` içeriğinin de gerçekten değişmez tutulup tutulmadığı uygulama kodundan kontrol edilmelidir.

### İstenen sekiz parser senaryosu

1. Geçerli satır.
2. Eksik alan.
3. Hatalı zaman damgası.
4. Hatalı HTTP durum kodu.
5. Boş satır.
6. Fazladan boşluk.
7. Boşluk içeren tırnaklı user-agent.
8. Sorgu dizesi (`query string`) içeren yol.

**Kapsama:** Önceki Artım 2 çalışmasında JaCoCo satır kapsama oranı **%76,24** olarak raporlanmıştır. Bu oran o ölçümün sonucudur; Artım 4 için güncel kapsama oranı olarak yorumlanmamalıdır.

**Ödev teslimleri:** `v2` etiketi, `LogRecord`, `ParserStage`, en az sekiz parser testi, kapsama yüzdesi ve değiştirilen dosyaları listeleyen `CHANGELOG.md` kaydı.

## 6. Artım 3 — Filtreleme, Projeksiyon ve Pipeline Builder

**Mimari odak:** Modülerlik, bileşilebilirlik ve aşama düzeyinde tek sorumluluk.

### Yeni aşamalar

- **`PredicateFilterStage`:** `status >= 400`, `path.startsWith("/api")` veya zaman aralığı gibi koşulları karşılayan kayıtları geçirir.
- **`ProjectionStage`:** Seçilen alanları istenen sırada üretir. Gereksiz veri aktarımını azaltır (veri minimizasyonu).
- **`FileSink`:** Çıktı dosyasını açar, yazar ve kapatır. `open()` / `close()` kaynak yönetimi açısından zorunludur.

Ödev, bu üç aşamanın her birinin **yaklaşık 40 satırın altında** tutulmasını istemektedir. Bu, kod üzerinde ayrıca kontrol edilmesi gereken bir kalite ölçütüdür.

### Akıcı builder örneği

```java
Pipeline pipeline = PipelineBuilder
    .from(new FileLineSource("data/access-small.log"))
    .via(new ParserStage())
    .via(new PredicateFilterStage(r -> r.status() >= 400))
    .via(new ProjectionStage("timestamp", "clientIp", "status", "path"))
    .to(new FileSink<>("output.log"));

pipeline.run();
```

### Komut satırı (CLI)

```powershell
java -cp "out;lib/*" Main data/access-small.log --min-status 400 --output output.log
```

```powershell
java -cp "out;lib/*" Main data/access-small.log --path-prefix /page19 --fields clientIp,status,path --output output.log
```

Desteklenen seçenekler:

| Parametre | Anlamı |
| --- | --- |
| `--min-status 400` | HTTP durum kodu en az 400 olan kayıtlar |
| `--path-prefix /api` | Yolu `/api` ile başlayan kayıtlar |
| `--fields timestamp,clientIp,status` | Çıktı alanları ve sırası |
| `--output output.log` | Çıktı dosyası |

**Filtre sıralaması deneyi (zorunlu):** Aynı veri, örneğin durum kodu filtresi → yol filtresi ve yol filtresi → durum kodu filtresi sıralarında çalıştırılmalı; **aynı çıktı kayıtları** ve **ölçülmüş performans farkları** `ARCHITECTURE.md` dosyasında belgelenmelidir. Filtreler, ayrıştırılmış `LogRecord` üzerinde çalıştığından parser'dan önce uygulanamaz. Ölçülmemiş süreler bu belgeye eklenmemiştir.

**Testler:** Üç yeni aşama için ayrı birim testleri ve küçük fixture üzerinde dosya içeriğini doğrulayan entegrasyon testi.

**Ödev teslimleri:** `v3` etiketi; üç aşama, `PipelineBuilder`, CLI argüman işleme ve `ARCHITECTURE.md` içinde filtre sıralaması deney raporu.

## 7. Artım 4 — Yapılandırma Odaklı Birleştirme ve Aşama Kaydı

**Mimari odak:** Genişletilebilirlik; fabrika/kayıt defteri tasarım kalıbı; mimari tanımın gerçekleştirimden ayrılması.

### Amaç

Pipeline topolojisini Java koduna sabitlemek yerine YAML dosyasında tanımlamak. **Farklı pipeline kurulumları için Java kodunu değiştirmeden ve yeniden derlemeden** aynı derlenmiş uygulamayı çalıştırmak.

### Bileşenler ve sorumluluklar

- `YamlConfigLoader`: Dosyanın varlığını kontrol eder; SnakeYAML ile içeriği yükler; kök yapıyı ve anahtar türlerini doğrular.
- `StageFactory`: Yapılandırma parametrelerini alarak ilgili aşama nesnesini üretir.
- `StageRegistry`: İsim–fabrika eşleştirmesini tutar; bilinmeyen aşamaları reddeder.
- `DefaultStageRegistry`: Varsayılan parser, filter ve projection fabrikalarını kaydeder.
- `PipelineConfigBuilder`: Source, aşamalar ve sink bilgilerini doğrular; çalıştırılabilir `Pipeline` oluşturur. Projede bu bileşen, ödevde **PipelineLoader** olarak ifade edilen sorumluluğu üstlenir.

**Tasarım notu:** Kayıt mekanizması veri odaklı bir kayıt tablosudur. Yeni bir aşamanın kayıt edilmesi, `StageRegistry` içine yeni bir `switch` kolu yazılmasını gerektirmemelidir.

### Projede kullanılan YAML biçimi

Aşağıdaki örnek, paylaşılan `PipelineConfigBuilder` kodunun desteklediği **düz (flat)** `source` / `stages` / `sink` yapısına uygundur:

```yaml
source:
  type: file
  path: data/access-small.log

stages:
  - type: parser
  - type: filter
    minStatus: 400
  - type: projection
    fields: [timestamp, clientIp, status, path]

sink:
  type: file
  path: output.log
```

| YAML alanı | Tür / kural | Açıklama |
| --- | --- | --- |
| `source` | Nesne, zorunlu | Kaynak tanımı |
| `source.type` | `file` | Desteklenen kaynak türü |
| `source.path` | Boş olmayan metin | Okunacak dosya |
| `stages` | Liste | Sıralı aşama tanımları |
| `stages[].type` | `parser`, `filter`, `projection` | Aşama türü |
| `filter.minStatus` | Sayı, isteğe bağlı | Alt HTTP durum kodu sınırı |
| `filter.pathPrefix` | Boş olmayan metin, isteğe bağlı | URL yolu öneki |
| `projection.fields` | Boş olmayan metin listesi | Seçilecek alanlar |
| `sink.type` | `file` | Desteklenen çıktı türü |
| `sink.path` | Boş olmayan metin | Çıktı dosyası |

`filter` aşamasında `minStatus` ve `pathPrefix` değerlerinden **en az biri** bulunmalıdır; ikisi varsa koşullar birlikte uygulanır. `projection.fields` alanları `LogRecord` üzerinde tanımlı olmalıdır.

> **Ödev örneğiyle fark:** Öğretmenin PDF'inde üst düzey `pipeline:`, aşama `params:` nesneleri ve `parse` / `project` adları gösterilir. Paylaşılan mevcut kodda ise üst düzey `source`, `stages`, `sink` ve `parser` / `projection` adları kullanılıyor. Bu README **çalıştığı doğrulanan mevcut uygulamanın şemasını** belgeler. Hocanın örnek şemasına birebir uyum ayrıca isteniyorsa kodda uyarlama gerekir.

### Üç ayrı yapılandırma

Aynı derlenmiş uygulama aşağıdaki üç dosyayla çalıştırılmıştır:

| Dosya | Amaç |
| --- | --- |
| `configs/error-report.yaml` | HTTP hata kayıtlarının seçimi |
| `configs/api-traffic.yaml` | API isteklerinin seçimi |
| `configs/bandwidth-by-client.yaml` | `clientIp` ve `bytes` alanlarının çıkarılması |

```powershell
java -cp "out;lib/*" Main --config configs/error-report.yaml
java -cp "out;lib/*" Main --config configs/api-traffic.yaml
java -cp "out;lib/*" Main --config configs/bandwidth-by-client.yaml
```

Başarılı çalıştırma örneği:

```text
YAML pipeline basariyla calisti: configs/error-report.yaml
```

**Önemli sınır:** `bandwidth-by-client.yaml` mevcut haliyle IP ve bayt alanlarını **kayıt başına** çıkarır; istemciye göre toplam bayt hesaplayan bir `group by` / toplama aşaması gösterilmemiştir. `api-traffic.yaml` çıktısının boş olması, örnek veride filtreyle eşleşen kayıt bulunmamasından kaynaklanabilir.

### Parametre doğrulama ve hata yönetimi

Yapılandırma zamanı hataları, kayıtlar işlenmeden önce açıkça bildirilmelidir (**fail fast, fail loudly**). Bu artımda çalışma zamanı bozuk veri işleme politikasının genişletilmesi amaçlanmaz.

Gerçek çalıştırmalarda gözlenen örnekler:

```powershell
java -cp "out;lib/*" Main --config configs/olmayan-dosya.yaml
```

```text
Parametre hatasi: YAML dosyasi bulunamadi: configs/olmayan-dosya.yaml
```

```powershell
java -cp "out;lib/*" Main --config configs/invalid-stage.yaml
```

```text
Parametre hatasi: Bilinmeyen asama turu: bilinmeyen
```

```powershell
java -cp "out;lib/*" Main --config configs/invalid-filter.yaml
```

```text
Parametre hatasi: Filter icin minStatus veya pathPrefix gerekli.
```

```powershell
java -cp "out;lib/*" Main --config configs/invalid-field.yaml
```

```text
Parametre hatasi: Gecersiz veya bos alan secimi.
```

**Ödevde istenen beş yükleyici senaryosu doğrulandı:** geçerli yapılandırma, bilinmeyen aşama, eksik parametre, yanlış parametre türü ve boş pipeline. `PipelineConfigBuilderTest` içindeki altı testin tamamı başarılıdır (ek olarak geçersiz projeksiyon alanı test edilir). Boş `stages: []` listesi, işlem başlamadan önce `IllegalArgumentException` ile reddedilir.

**Ödev teslimleri:** `v4` etiketi, `ARCHITECTURE.md` içinde yapılandırma şeması, stage registry, loader/builder, üç YAML dosyası ve README içinde hata örnekleri.

## 8. Klasör yapısı

```text
LogFlow/
├── src/
│   ├── Main.java
│   ├── Stage.java, Emitter.java, Source.java, Sink.java
│   ├── FileLineSource.java, ConsoleSink.java
│   ├── Pipeline.java, PipelineBuilder.java
│   ├── LogRecord.java, ParserStage.java
│   ├── PredicateFilterStage.java, ProjectionStage.java, FileSink.java
│   ├── StageFactory.java, StageRegistry.java, DefaultStageRegistry.java
│   ├── YamlConfigLoader.java, PipelineConfigBuilder.java
│   └── ...
├── data/
│   └── access-small.log
├── configs/
│   ├── error-report.yaml
│   ├── api-traffic.yaml
│   ├── bandwidth-by-client.yaml
│   └── invalid-*.yaml            # Test amaçlı hatalı yapılandırmalar
├── test/
│   ├── ParserStageTest.java
│   ├── PredicateFilterStageTest.java
│   ├── ProjectionStageTest.java
│   ├── FileSinkTest.java
│   ├── PipelineIntegrationTest.java
│   ├── YamlConfigLoaderTest.java
│   ├── StageRegistryTest.java
│   ├── PipelineConfigBuilderTest.java
│   ├── YamlPipelineIntegrationTest.java
│   └── ...
├── lib/
│   ├── snakeyaml-2.7.jar
│   └── junit-platform-console-standalone-6.1.3.jar
├── out/                         # Derleme çıktısı
├── README.md
├── ARCHITECTURE.md
└── CHANGELOG.md
```

Bu ağaç, artımlarda kullanılan **beklenen ve konuşmada görülen dosyaları** gösterir; yerel klasörün eksiksiz envanteri değildir. `ARCHITECTURE.md` ve `CHANGELOG.md` dosyaları Artım 4 sonuçlarına göre güncellenmiştir.

## 9. Kurulum ve çalıştırma

### 9.1 Gereksinimler

- JDK 17 veya üzeri (`java -version`, `javac -version`)
- `lib/snakeyaml-2.7.jar`
- Testler için `lib/junit-platform-console-standalone-6.1.3.jar`

### 9.2 Derleme

```powershell
javac -cp "lib/*" -d out src\*.java
```

### 9.3 Artım 3 CLI çalıştırma

```powershell
java -cp "out;lib/*" Main data/access-small.log --min-status 400 --output output.log
Get-Content output.log
```

### 9.4 Artım 4 YAML çalıştırma

```powershell
java -cp "out;lib/*" Main --config configs/error-report.yaml
```

YAML dosyasında yalnızca yapılandırma değiştirilerek farklı iş hatları tanımlanabilir; Java kaynak kodunun tekrar derlenmesi gerekmez.

### 9.5 Örnek projeksiyon çıktısı

```text
{clientIp=10.0.0.5, status=403, path=/admin}
{clientIp=192.168.1.10, status=500, path=/api/users}
```

Bu satırlar entegrasyon testi örneğindeki kayıt biçimini gösterir. Filtreyle eşleşme yoksa çıktı dosyası boş olabilir.

## 10. Otomatik testler ve doğrulama

### Testleri derleme

```powershell
javac -cp "out;lib/*" -d out test\*.java
```

### Tüm testleri çalıştırma

```powershell
java -jar lib\junit-platform-console-standalone-6.1.3.jar execute --class-path "out;lib/snakeyaml-2.7.jar" --scan-class-path
```

### Son paylaşılan toplu test sonucu

| Ölçüt | Sonuç |
| --- | ---: |
| Bulunan test | **50** |
| Başarılı test | **50** |
| Başarısız test | **0** |
| Atlanan test | **0** |
| Başarılı test konteyneri | **13** |

Testler parser, filtre, projeksiyon, dosya yazma, pipeline, YAML loader, registry, yapılandırma builder'ı ve YAML entegrasyonunu kapsar. **50/50 sonucu yalnızca gösterilen test çalıştırmasını ifade eder; tüm ödev koşullarının eksiksiz karşılandığını tek başına kanıtlamaz.**

## 11. Mimari belgeler ve sürüm yönetimi

Öğretim elemanının teslim koşulları doğrultusunda aşağıdaki belgeler ve sürüm etiketleri takip edilmelidir:

| Artım | Git etiketi | Özel belge / kanıt |
| --- | --- | --- |
| 1 | `v1` | `ARCHITECTURE.md`: iki kutulu ilk diyagram |
| 2 | `v2` | `README.md`: JaCoCo satır kapsama oranı; `CHANGELOG.md`: değişen dosyalar |
| 3 | `v3` | `ARCHITECTURE.md`: iki filtre sırası, aynı çıktı ve ölçülen performans farkı |
| 4 | `v4` | `ARCHITECTURE.md`: YAML yapılandırma şeması; registry, loader, üç yapılandırma |

Etiketlerin gerçekten oluşturulup uzak depoya gönderildiği `git tag` ve `git ls-remote --tags origin` ile kontrol edilmelidir. README'deki teslim listesi, etiketlerin mevcut olduğunu iddia etmez.


## 12. Mimari sınırlamalar

- `ProjectionStage` alan seçer; gruplama veya istemci başına toplam bayt hesaplamaz.
- Aşamaların yer değiştirmesi, veri türü uyumluluğu ve filtrelerin semantiği dikkate alınarak yapılmalıdır.
- Artım 4'teki yapılandırma hatalarıyla Artım 5'e bırakılan çalışma zamanı veri hataları farklı sorumluluklardır.
- Bu belge, paylaşılan kod ve terminal sonuçları ile dört ödev yönergesine göre hazırlanmıştır; proje deposunun tamamına erişilerek yapılmış bir kod denetimi değildir.

---

**LogFlow — Yazılım Mimarisi Dönem Projesi | Artım 1–4**
