
# LogFlow - CHANGELOG

Bu dosya, LogFlow projesinde surumlere gore yapilan
degisiklikleri ve eklenen ozellikleri belgelemektedir.

Proje, Yazilim Mimarisi dersi kapsaminda
artimli gelistirme yaklasimiyla gelistirilmistir.

---

## [v3] - Artim 3: Filtreleme, Projeksiyon ve PipelineBuilder

### Eklenen Ozellikler

- PredicateFilterStage sinifi eklendi.
- Predicate<LogRecord> ile kosullu filtreleme saglandi.
- HTTP durum koduna gore filtreleme eklendi.
- URL yolu on ekine gore filtreleme eklendi.
- Zaman araligina gore filtreleme desteklendi.
- ProjectionStage sinifi eklendi.
- LogRecord alanlarinin secilerek aktarilmasi saglandi.
- FileSink sinifi eklendi.
- PipelineBuilder sinifi eklendi.
- Fluent API ile pipeline olusturma desteklendi.
- Komut satiri parametreleri eklendi.
- Filtre siralamasi deneyi gerceklestirildi.
- ARCHITECTURE.md dokumantasyonu hazirlandi.

### Komut Satiri Parametreleri

- --min-status
- --path-prefix
- --fields
- --output

### Mimari Degisiklikler

- Pipeline sinifi birden fazla Stage destekleyecek
  sekilde yeniden duzenlendi.
- Main.java, PipelineBuilder kullanacak sekilde guncellendi.
- Sink arayuzune open() ve close() metotlari eklendi.
- Veri isleme asamalari fluent API ile
  birlestirilebilir hale getirildi.

### Eklenen Testler

- PredicateFilterStageTest: 7 test
- ProjectionStageTest: 8 test
- FileSinkTest: 7 test
- PipelineIntegrationTest: 1 test

Artim 3 kapsaminda 23 yeni test eklendi.

### Test Sonuclari

Emine'nin dogrulanan JUnit calistirmasinda:

- Toplam test: 32
- Basarili test: 32
- Basarisiz test: 0

### Filtre Siralamasi Deneyi

200 kayitlik test veri seti kullanildi.

Karsilastirilan siralamalar:

1. Status -> Path
2. Path -> Status

Sonuclar:

| Olcum | Status -> Path | Path -> Status |
|---|---:|---:|
| Cikti kaydi | 20 | 20 |
| Status kontrolu | 200 | 100 |
| Path kontrolu | 40 | 200 |
| Toplam kontrol | 240 | 300 |

Her iki siralama ayni ciktiyi uretmistir.

Status filtresinin once kullanilmasi,
bu veri setinde toplam filtre kontrol
sayisini %20 azaltmistir.

Tek calistirmadaki sure farklari,
kesin performans ustunlugu olarak
degerlendirilmemistir.

---

## [v2] - Artim 2: LogRecord ve ParserStage

### Eklenen Ozellikler

- LogRecord veri modeli eklendi.
- Java record yapisi kullanildi.
- ParserStage sinifi gelistirildi.
- CLF formatindaki log satirlari ayrıştırildi.
- HTTP method, path, status ve bytes
  alanlari yapilandirilmis hale getirildi.
- Gecersiz log satirlarini sayma ozelligi eklendi.
- getErrorCount() metodu eklendi.

### Testler

- ParserStage icin 8 birim testi yazildi.
- Pipeline icin 1 test yazildi.
- JUnit test altyapisi kullanildi.
- JaCoCo ile kod kapsama analizi yapildi.

### Kod Kapsama Sonucu

JaCoCo satir kapsama orani: %76,24

### Iyilestirmeler

- Log satirlarinin yapilandirilmis
  nesnelere donusturulmesi saglandi.
- Parser hatalarinin izlenebilirligi artirildi.
- Pipeline davranislari testlerle kontrol edildi.

---

## [v1] - Artim 1: Temel Pipeline Mimarisi

### Eklenen Ozellikler

- Source arayuzu olusturuldu.
- Stage arayuzu olusturuldu.
- Sink arayuzu olusturuldu.
- Emitter arayuzu olusturuldu.
- Pipeline sinifi gelistirildi.
- FileLineSource sinifi eklendi.
- ConsoleSink sinifi eklendi.

### Mimari Yapi

Source - Stage - Sink yaklasimi kullanildi.

Temel veri akisi:

    FileLineSource
          |
          v
        Stage
          |
          v
      ConsoleSink

### Kazanimlar

- Moduler veri isleme altyapisi olusturuldu.
- Bilesenler arasinda gorev ayrimi saglandi.
- Pipeline mimarisinin temeli atildi.
- Sonraki artimlar icin genisletilebilir
  bir yapi hazirlandi.

---

## Surum Ozeti

| Surum | Temel Gelistirmeler |
|---|---|
| v1 | Source, Stage, Sink ve temel Pipeline |
| v2 | LogRecord, ParserStage, JUnit ve JaCoCo |
| v3 | Filtreleme, projeksiyon, FileSink ve PipelineBuilder |

Not: v3 Git etiketi, Artim 3 kodlari ve
dokumantasyonu GitHub'a gonderildikten
sonra olusturulacaktir.
