
# LogFlow
### Java Tabanli Log Isleme Sistemi

LogFlow, log dosyalarini okumak, ayrıştırmak, filtrelemek
ve islenen verileri farkli ciktilara aktarmak icin
gelistirilmis Java tabanli bir uygulamadir.

Proje, Yazilim Mimarisi dersi kapsaminda
artimli gelistirme yaklasimiyla hazirlanmistir.

---

## 1. Projenin Amaci

LogFlow projesinin temel amaci, log dosyalarinin
moduler ve genisletilebilir bir mimariyle
islenmesini saglamaktir.

Uygulama sayesinde:

- Log dosyalari satir satir okunabilir.
- Log satirlari yapilandirilmis nesnelere donusturulebilir.
- Gecersiz log kayitlari tespit edilebilir.
- Kayitlar belirli kosullara gore filtrelenebilir.
- Istenen veri alanlari secilebilir.
- Sonuclar konsola veya dosyaya aktarilabilir.
- Veri isleme asamalari kolayca degistirilebilir.

---

## 2. Kullanilan Teknolojiler

| Teknoloji | Kullanim Amaci |
|---|---|
| Java | Uygulama gelistirme |
| Java Records | Log verilerinin modellenmesi |
| Java Generics | Tur guvenligi |
| Java Streams ve Predicate | Veri filtreleme |
| JUnit 5 | Birim ve entegrasyon testleri |
| JaCoCo | Kod kapsama analizi |
| Git | Surum kontrolu |
| GitHub | Proje paylasimi |
| VS Code | Gelistirme ortami |

---

## 3. Mimari Yapi

LogFlow, Source - Stage - Sink mimarisini kullanir.

### Veri Akisi

    FileLineSource
          |
          v
      ParserStage
          |
          v
    PredicateFilterStage
          |
          v
     ProjectionStage
          |
          v
        FileSink

### Source

Verinin sisteme girisini saglar.

FileLineSource, log dosyasini satir satir okur.

### Stage

Verinin islenmesinden sorumludur.

ParserStage:
Ham log satirlarini LogRecord nesnelerine donusturur.

PredicateFilterStage:
Belirtilen kosullara uyan kayitlari sonraki asamaya aktarir.

ProjectionStage:
LogRecord nesnesinden yalnizca istenen alanlari secer.

### Sink

Islenen verilerin ciktiya aktarilmasini saglar.

ConsoleSink:
Sonuclari konsolda gosterir.

FileSink:
Sonuclari belirtilen dosyaya yazar.

---

## 4. Proje Gelistirme Asamalari

### Artim 1 - Temel Pipeline Mimarisi

Ilk asamada temel veri isleme altyapisi olusturulmustur.

Gelistirilen bilesenler:

- Source
- Stage
- Sink
- Emitter
- Pipeline
- FileLineSource
- ConsoleSink

Bu asamada Source - Stage - Sink mimarisi
uygulanmistir.

### Artim 2 - Log Ayrıştırma ve Testler

Ikinci asamada log satirlarinin
yapilandirilmis verilere donusturulmesi saglanmistir.

Eklenen ozellikler:

- LogRecord veri modeli
- ParserStage
- CLF formatinda log ayrıştırma
- Gecersiz satir sayimi
- JUnit birim testleri
- Pipeline testleri
- JaCoCo kod kapsama analizi

Artim 2 calismasinda JaCoCo satir kapsama
orani %76,24 olarak olculmustur.

### Artim 3 - Filtreleme ve Projeksiyon

Ucuncu asamada pipeline mimarisi
yeni bilesenlerle genisletilmistir.

Eklenen ozellikler:

- PredicateFilterStage
- ProjectionStage
- FileSink
- PipelineBuilder
- Komut satiri parametreleri
- Filtre siralamasi deneyi
- Yeni birim ve entegrasyon testleri
- ARCHITECTURE.md dokumantasyonu

---

## 5. Proje Klasor Yapisi

    LogFlow/
    |
    |-- src/
    |   |-- Main.java
    |   |-- Source.java
    |   |-- Stage.java
    |   |-- Sink.java
    |   |-- Emitter.java
    |   |-- Pipeline.java
    |   |-- PipelineBuilder.java
    |   |-- FileLineSource.java
    |   |-- ParserStage.java
    |   |-- PredicateFilterStage.java
    |   |-- ProjectionStage.java
    |   |-- ConsoleSink.java
    |   |-- FileSink.java
    |   |-- LogRecord.java
    |   |-- FilterOrderExperiment.java
    |
    |-- test/
    |   |-- ParserStageTest.java
    |   |-- PipelineTest.java
    |   |-- PredicateFilterStageTest.java
    |   |-- ProjectionStageTest.java
    |   |-- FileSinkTest.java
    |   |-- PipelineIntegrationTest.java
    |
    |-- data/
    |   |-- access-small.log
    |   |-- filter-experiment.log
    |
    |-- lib/
    |   |-- junit-platform-console-standalone-6.1.3.jar
    |
    |-- out/
    |
    |-- README.md
    |-- CHANGELOG.md
    |-- ARCHITECTURE.md

Not: out klasoru derleme sirasinda olusturulur.
Diger yardimci dosyalar proje surumune gore degisebilir.

---

## 6. PipelineBuilder Kullanimi

Artim 3 ile eklenen PipelineBuilder,
pipeline asamalarinin fluent API ile
olusturulmasini saglar.

Ornek Java kodu:

    Pipeline pipeline = PipelineBuilder
        .from(new FileLineSource(inputFile))
        .via(new ParserStage())
        .via(new PredicateFilterStage(
            r -> r.status() >= 400
        ))
        .via(new ProjectionStage(
            "clientIp", "status", "path"
        ))
        .to(new FileSink<>(outputFile));

    pipeline.run();

Bu yaklasim sayesinde veri isleme
asamalari okunabilir ve genisletilebilir
bir sekilde birlestirilebilir.

---

## 7. Uygulamanin Derlenmesi

Projenin ana klasorunde terminali acin.

Asagidaki komutu calistirin:

    javac -d out src\*.java

Bu komut Java kaynak dosyalarini derler
ve out klasorune kaydeder.

---

## 8. Uygulamanin Calistirilmasi

Temel kullanim:

    java -cp out Main data\access-small.log

Durum koduna gore filtreleme:

    java -cp out Main data\access-small.log --min-status 400

URL yoluna gore filtreleme:

    java -cp out Main data\access-small.log --path-prefix /api

Alan secimi:

    java -cp out Main data\access-small.log --fields clientIp,status,path

Tum parametrelerle kullanim:

    java -cp out Main data\access-small.log --min-status 400 --path-prefix /api --fields clientIp,status,path --output output.log

---

## 9. Komut Satiri Parametreleri

| Parametre | Aciklama |
|---|---|
| --min-status | Minimum HTTP durum kodu |
| --path-prefix | URL yolu on eki |
| --fields | Ciktiya aktarilacak alanlar |
| --output | Cikti dosyasinin adi |

Ornek:

    --min-status 400

Bu parametre HTTP durum kodu
400 ve uzeri olan kayitlari secer.

Ornek:

    --path-prefix /api

Bu parametre URL yolu /api ile
baslayan kayitlari secer.

---

## 10. Birim ve Entegrasyon Testleri

LogFlow projesinde JUnit kullanilmaktadir.

### Testleri Derleme

    javac -cp "lib\junit-platform-console-standalone-6.1.3.jar;out" -d out test\*.java

### Testleri Calistirma

    java -jar lib\junit-platform-console-standalone-6.1.3.jar execute --class-path out --scan-class-path

### Test Dosyalari

| Test Sinifi | Test Sayisi |
|---|---:|
| ParserStageTest | 8 |
| PipelineTest | 1 |
| PredicateFilterStageTest | 7 |
| ProjectionStageTest | 8 |
| FileSinkTest | 7 |
| PipelineIntegrationTest | 1 |
| Toplam | 32 |

### Dogrulanan Test Sonucu

Emine'nin calistirdigi test paketinde:

- Bulunan test: 32
- Baslatilan test: 32
- Basarili test: 32
- Basarisiz test: 0

Tum testler basariyla tamamlanmistir.

---

## 11. Filtre Siralamasi Deneyi

Artim 3 kapsaminda filtre siralamasinin
islem maliyetine etkisi incelenmistir.

Karsilastirilan siralamalar:

1. Status Filter -> Path Filter
2. Path Filter -> Status Filter

Deney icin 200 kayitlik log dosyasi
kullanilmistir.

### Deneyi Calistirma

    java -cp out FilterOrderExperiment data\filter-experiment.log

### Deney Sonuclari

| Olcum | Status -> Path | Path -> Status |
|---|---:|---:|
| Girdi kaydi | 200 | 200 |
| Cikti kaydi | 20 | 20 |
| Status kontrolu | 200 | 100 |
| Path kontrolu | 40 | 200 |
| Toplam kontrol | 240 | 300 |
| Ayni cikti | Evet | Evet |

### Degerlendirme

Iki filtre siralamasi da ayni
20 log kaydini uretmistir.

Status filtresinin once kullanildigi
siralama 240 kontrol yapmistir.

Path filtresinin once kullanildigi
siralama 300 kontrol yapmistir.

Birinci siralama, toplam filtre
kontrol sayisini %20 azaltmistir.

Tek calistirmadaki sure olcumleri
kesin performans ustunlugu gostermedigi
icin filtre kontrol sayilari
temel karsilastirma olcutu olarak
degerlendirilmistir.

Ayrintili deney sonuclari
ARCHITECTURE.md dosyasinda yer almaktadir.

---

## 12. Mimari Tasarim Ilkeleri

LogFlow projesinde asagidaki
tasarim ilkeleri esas alinmistir.

### Single Responsibility Principle

Her bilesen belirli bir islemden
sorumludur.

### Open/Closed Principle

Yeni Stage siniflari eklenerek
pipeline genisletilebilir.

### Reusability

Filtreleme ve projeksiyon asamalari
farkli parametrelerle yeniden kullanilabilir.

### Type Safety

PipelineBuilder, asamalar arasinda
tur uyumlulugunun korunmasina yardimci olur.

### Resource Management

FileSink, open() ve close()
metotlariyla dosya kaynaklarini yonetir.

---

## 13. Dokumantasyon

Proje dokumantasyonu:

- README.md: Kurulum, kullanim ve genel bilgiler
- CHANGELOG.md: Surumlere gore degisiklikler
- ARCHITECTURE.md: Mimari kararlar ve deney sonuclari

---

## 14. Surumler

| Surum | Aciklama |
|---|---|
| v1 | Temel pipeline mimarisi |
| v2 | LogRecord, ParserStage ve testler |
| v3 | Filtreleme, projeksiyon, FileSink ve PipelineBuilder |

v3 etiketi, son Git islemleri tamamlandiginda
olusturulacaktir.

---

## 15. Sonuc

LogFlow, artimli gelistirme yaklasimiyla
olusturulmus moduler bir log isleme sistemidir.

Artim 1'de temel pipeline altyapisi,
Artim 2'de log ayrıştırma ve test altyapisi,
Artim 3'te ise filtreleme, projeksiyon,
dosyaya yazma ve fluent API
ozellikleri gelistirilmistir.

Proje, yeni veri isleme asamalarinin
eklenmesine uygun bir mimari sunmaktadir.
