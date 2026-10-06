# Change Log

## v2 – Artım 2: Tipli Kayıtlar ve İlk Gerçek Filtre

### Eklenen Dosyalar
- `src/LogRecord.java`
- `src/ParserStage.java`
- `test/CollectingEmitter.java`
- `test/ParserStageTest.java`
- `test/PipelineTest.java`

### Değiştirilen Dosyalar
- `src/Pipeline.java`
- `src/ConsoleSink.java`
- `src/Main.java`
- `.gitignore`
- `README.md`

### Yapılan Değişiklikler
- Immutable `LogRecord` alan modeli eklendi.
- Common Log Format (CLF) kayıtlarını ayrıştıran `ParserStage` eklendi.
- Pipeline, `LogRecord` nesneleriyle çalışacak şekilde güncellendi.
- `ConsoleSink`, yapılandırılmış kayıtları gösterecek şekilde güncellendi.
- ParserStage için 8 birim testi oluşturuldu.
- Pipeline için 1 birim testi oluşturuldu.
- Testlerde `CollectingEmitter` test double kullanıldı.
- Toplam 9 test başarıyla tamamlandı.
- JaCoCo ile kod kapsama analizi gerçekleştirildi.
- Line Coverage: %76,24.