# 📏 Length Converter – 7 языков, безграничные измерения

**Length Converter** – коллекция из семи независимых реализаций конвертера единиц длины. Каждая версия работает на своём языке программирования и поддерживает полный набор метрических и имперских единиц.

## ✨ Особенности

- 🌍 **Поддерживаемые единицы**:  
  мм, см, дм, м, км, дюймы (in), футы (ft), ярды (yd), мили (mi), морские мили (nmi)
- 🔄 **Преобразование** между любыми двумя единицами
- 📂 **Пакетный режим** – конвертация списка значений из файла
- 📊 **Таблица эквивалентов** – генерация диапазона для визуализации
- 💾 **Экспорт результатов** в CSV/TXT
- 🖥️ **Интерфейсы**: CLI (все) + GUI (Python, Java, C#)
- ⚡ **Высокая точность** (до 10 знаков после запятой)

## 🚀 Быстрый старт

### Установка зависимостей

#### Python
```bash
pip install tkinter  # (опционально для GUI)

JavaScript (Node.js)
bash
npm install commander
Go
bash
go get -u
Rust
bash
cargo add clap
Java
Скачайте json.jar (если нужен JSON-экспорт) или используйте встроенный org.json.

C#
Установите Newtonsoft.Json через NuGet.

PHP
Не требует внешних библиотек.

Использование
CLI (общий синтаксис):

bash
python length_convert.py --value 100 --from m --to km --precision 3
Примеры:

Конвертировать 5 миль в километры:

bash
go run length_convert.go -value 5 -from mi -to km
Сгенерировать таблицу от 0 до 10 метров с шагом 0.5:

bash
node length_convert.js --range 0,10,0.5 --from m --to ft
Пакетная обработка из файла values.txt:

bash
php length_convert.php --batch values.txt --from cm --to in --output results.csv
📋 Список единиц
Код	Единица	Код	Единица
mm	Миллиметр	in	Дюйм
cm	Сантиметр	ft	Фут
dm	Дециметр	yd	Ярд
m	Метр	mi	Миля
km	Километр	nmi	Морская миля
📁 Структура репозитория
text
length-converter/
├── README.md
├── length_convert.py      # Python (CLI + Tkinter GUI)
├── length_convert.js      # JavaScript (Node.js CLI)
├── length_convert.go      # Go (CLI)
├── length_convert.rs      # Rust (CLI)
├── LengthConverter.java   # Java (CLI + Swing GUI)
├── LengthConverter.cs     # C# (CLI + WinForms)
└── length_convert.php     # PHP (CLI + веб)
📜 Лицензия
MIT – используйте и модифицируйте свободно.
