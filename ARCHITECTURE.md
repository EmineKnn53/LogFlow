
# LogFlow - Mimari Dokumantasyon

## 1. Proje Genel Bakisi

LogFlow, Java programlama dili kullanilarak gelistirilen,
log dosyalarini asamali olarak isleyen bir uygulamadir.

Proje, Source - Stage - Sink mimari yaklasimini temel alir.

Artim 3 kapsaminda uygulamaya su ozellikler eklenmistir:

- Predicate tabanli filtreleme
- Istenen alanlari secme (Projection)
- Dosyaya cikti yazma
- Fluent PipelineBuilder API
- Komut satiri parametreleri
- Birim ve entegrasyon testleri
- Filtre siralamasi ve performans deneyi

## 2. Sistemin Mimari Yapisi

Uygulamanin genel veri akisi:

```text
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
```

### 2.1. FileLineSource

Log dosyasini satir satir okuyarak pipeline'a aktarir.

### 2.2. ParserStage

Ham log satirlarini LogRecord nesnelerine donusturur.

Gecersiz log satirlarini sayar ve hata sayisini
getErrorCount() metodu ile sunar.

### 2.3. PredicateFilterStage

Predicate<LogRecord> arayuzunu kullanarak
log kayitlarini belirli kosullara gore filtreler.

Desteklenen ornek kosullar:

- HTTP status >= 400
- URL yolunun /api ile baslamasi
- Belirli bir zaman araliginda bulunma

Kosulu saglayan kayitlar sonraki asamaya aktarilir.
Kosulu saglamayan kayitlar elenir.

### 2.4. ProjectionStage

LogRecord nesnesinden yalnizca kullanicinin
sectigi alanlari aktarir.

Ornek alanlar:

- timestamp
- clientIp
- method
- path
- status
- bytes
- userAgent
- attributes
- raw

Secilen alanlar LinkedHashMap kullanilarak
belirtilen sirada tutulur.

### 2.5. FileSink

Pipeline sonucunda olusan kayitlari
belirtilen cikti dosyasina yazar.

Temel metotlari:

- open(): Cikti dosyasini acar.
- consume(): Gelen kaydi dosyaya yazar.
- close(): Dosyayi kapatir.

Dosya yazma islemlerinde BufferedWriter kullanilir.

## 3. PipelineBuilder Tasarimi

PipelineBuilder, pipeline asamalarinin
fluent API yaklasimiyla birlestirilmesini saglar.

Ornek kullanim:

```java
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
```

PipelineBuilder kullaniminin avantajlari:

- Okunabilir kod yapisi
- Yeni asamalarin kolay eklenmesi
- Asamalarin yeniden kullanilabilmesi
- Asamalar arasinda tur uyumlulugunun kontrolu
- Pipeline olusturma isleminin sadeleştirilmesi

## 4. Komut Satiri Arayuzu

Main.java, pipeline'i PipelineBuilder ile olusturur.

Desteklenen parametreler:

### --min-status

Minimum HTTP durum kodunu belirler.

Ornek:

--min-status 400

### --path-prefix

URL yolunun hangi ifade ile baslamasi
gerektigini belirler.

Ornek:

--path-prefix /api

### --fields

Ciktiya aktarilacak alanlari belirler.

Ornek:

--fields clientIp,status,path

### --output

Cikti dosyasinin adini belirler.

Ornek:

--output output.log

Tam calistirma ornegi:

```powershell
java -cp out Main data\access-small.log --min-status 400 --path-prefix /api --fields clientIp,status,path --output output.log
```

## 5. Filtre Siralamasi Deneyi

### 5.1. Deneyin Amaci

Filtrelerin uygulanma sirasinin
sonuc dogruluguna ve islem maliyetine
etkisini incelemek.

### 5.2. Kullanilan Veri Seti

Deney icin 200 adet CLF formatinda
log kaydi olusturulmustur.

Veri setinde:

- HTTP 200 durum kodu
- HTTP 500 durum kodu
- /api/users URL yolu
- /home URL yolu

bulunmaktadir.

Dosya adi:

data/filter-experiment.log

### 5.3. Birinci Siralama

```text
FileLineSource
      |
      v
ParserStage
      |
      v
Status Filter
      |
      v
Path Filter
      |
      v
ProjectionStage
      |
      v
Sink
```

Bu siralamada once HTTP durum kodu,
ardindan URL yolu kontrol edilir.

### 5.4. Ikinci Siralama

```text
FileLineSource
      |
      v
ParserStage
      |
      v
Path Filter
      |
      v
Status Filter
      |
      v
ProjectionStage
      |
      v
Sink
```

Bu siralamada once URL yolu,
ardindan HTTP durum kodu kontrol edilir.

### 5.5. Deney Sonuclari

| Olcum | Status -> Path | Path -> Status |
|---|---:|---:|
| Girdi kaydi | 200 | 200 |
| Cikti kaydi | 20 | 20 |
| Status kontrolu | 200 | 100 |
| Path kontrolu | 40 | 200 |
| Toplam kontrol | 240 | 300 |
| Ayni cikti | Evet | Evet |

Paylasilan deney calistirmasinda olculen sureler:

| Siralama | Sure |
|---|---:|
| Status -> Path | 90.222 ms |
| Path -> Status | 11.791 ms |

Bu sureler tek bir calistirmadan elde edilmistir.
JVM isinmasi, sistem yuku ve diger etkenler
sureleri degistirebilir.

Bu nedenle sure sonuclari kesin bir
performans ustunlugu olarak yorumlanmamalidir.

### 5.6. Deneyin Degerlendirilmesi

Iki filtre siralamasi da ayni 20 kaydi uretmistir.

Status filtresinin once kullanildigi
siralama toplam 240 kontrol yapmistir.

Path filtresinin once kullanildigi
siralama toplam 300 kontrol yapmistir.

Birinci siralamada 60 daha az kontrol
gerceklestirilmistir.

Bu, toplam filtre kontrol sayisinda
%20 azalma anlamina gelmektedir.

Deney, filtre siralamasinin sonuc
dogrulugunu degistirmeden islem
maliyetini etkileyebildigini gostermistir.

## 6. Test Stratejisi

Artim 3 kapsaminda yeni birim
ve entegrasyon testleri eklenmistir.

### PredicateFilterStageTest

7 test bulunmaktadir.

Status, path ve timestamp filtreleme
davranislari kontrol edilmektedir.

### ProjectionStageTest

8 test bulunmaktadir.

Alan secimi, alan sirasi, gecersiz alanlar
ve veri dogrulugu test edilmektedir.

### FileSinkTest

7 test bulunmaktadir.

Dosya acma, veri yazma, dosya kapatma
ve hatali kullanim durumlari test edilmektedir.

### PipelineIntegrationTest

1 entegrasyon testi bulunmaktadir.

ParserStage, PredicateFilterStage ve
ProjectionStage birlikte calistirilarak
FileSink dosya ciktisi kontrol edilmektedir.

### Test Sonuclari

Emine'nin JUnit calistirmasinda:

- Toplam test: 32
- Basarili test: 32
- Basarisiz test: 0

Bu sonuc, mevcut test senaryolarinin
basariyla tamamlandigini gostermektedir.

## 7. Mimari Tasarim Ilkeleri

### 7.1. Single Responsibility Principle

Her bileşen belirli bir gorevden sorumludur.

ParserStage log ayrıştırma,
PredicateFilterStage filtreleme,
ProjectionStage alan secme,
FileSink ise dosyaya yazma
gorevini ustlenir.

### 7.2. Open/Closed Principle

Yeni Stage siniflari eklenerek
pipeline genisletilebilir.

Mevcut asamalarin her yeni gereksinim
icin degistirilmesi gerekmez.

### 7.3. Yeniden Kullanilabilirlik

PredicateFilterStage farkli Predicate
kosullariyla yeniden kullanilabilir.

ProjectionStage farkli alan secimleriyle
calistirilabilir.

### 7.4. Tur Guvenligi

PipelineBuilder'in via() metodu
asamalarin giris ve cikis turleri
arasindaki uyumu derleme zamaninda
kontrol etmeye yardimci olur.

### 7.5. Kaynak Yonetimi

FileSink, open() ve close() metotlari
ile dosya kaynaklarini yonetir.

Pipeline, asamalarin yasam dongusu
metotlarini calistirir.

## 8. Sonuc

Artim 3 ile LogFlow projesi
daha esnek ve genisletilebilir
bir pipeline mimarisine kavusmustur.

Yeni filtreleme, projeksiyon ve
dosyaya yazma ozellikleri eklenmistir.

PipelineBuilder sayesinde veri isleme
asamalari akici bir API ile
birlestirilebilir hale gelmistir.

JUnit testleriyle temel davranislar
ve entegrasyon senaryosu dogrulanmistir.

Filtre siralamasi deneyi sonucunda,
ayni ciktiyi ureten farkli siralamalarin
farkli sayida filtre kontrolu
gerceklestirebildigi gosterilmistir.
```
