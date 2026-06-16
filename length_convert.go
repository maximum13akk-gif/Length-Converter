// length_convert.go - Конвертер длин на Go (CLI)
package main

import (
	"bufio"
	"flag"
	"fmt"
	"os"
	"strconv"
	"strings"
)

type UnitMap map[string]float64

var units = UnitMap{
	"mm":  0.001,
	"cm":  0.01,
	"dm":  0.1,
	"m":   1.0,
	"km":  1000.0,
	"in":  0.0254,
	"ft":  0.3048,
	"yd":  0.9144,
	"mi":  1609.344,
	"nmi": 1852.0,
}

var unitNames = map[string]string{
	"mm":  "Миллиметр",
	"cm":  "Сантиметр",
	"dm":  "Дециметр",
	"m":   "Метр",
	"km":  "Километр",
	"in":  "Дюйм",
	"ft":  "Фут",
	"yd":  "Ярд",
	"mi":  "Миля",
	"nmi": "Морская миля",
}

func convert(value float64, from, to string) float64 {
	if from == to {
		return value
	}
	meters := value * units[from]
	return meters / units[to]
}

func main() {
	var (
		value     float64
		from      string
		to        string
		precision int
		batch     string
		output    string
		list      bool
		rangeArgs string
	)
	flag.Float64Var(&value, "value", 0, "Значение")
	flag.StringVar(&from, "from", "m", "Исходная единица")
	flag.StringVar(&to, "to", "km", "Целевая единица")
	flag.IntVar(&precision, "precision", 2, "Точность")
	flag.StringVar(&batch, "batch", "", "Файл со значениями")
	flag.StringVar(&output, "output", "", "Выходной файл")
	flag.BoolVar(&list, "list", false, "Список единиц")
	flag.StringVar(&rangeArgs, "range", "", "Диапазон: start,end,step")
	flag.Parse()

	if list {
		fmt.Println("Доступные единицы:")
		for code, name := range unitNames {
			fmt.Printf("  %s: %s\n", code, name)
		}
		return
	}

	if rangeArgs != "" {
		parts := strings.Split(rangeArgs, ",")
		if len(parts) != 3 {
			fmt.Println("Формат: start,end,step")
			return
		}
		start, _ := strconv.ParseFloat(parts[0], 64)
		end, _ := strconv.ParseFloat(parts[1], 64)
		step, _ := strconv.ParseFloat(parts[2], 64)
		if step <= 0 {
			fmt.Println("Шаг должен быть положительным")
			return
		}
		var rows []string
		for v := start; v <= end+1e-9; v += step {
			res := convert(v, from, to)
			rows = append(rows, fmt.Sprintf("%.*f %s = %.*f %s", precision, v, unitNames[from], precision, res, unitNames[to]))
		}
		if output != "" {
			os.WriteFile(output, []byte(strings.Join(rows, "\n")), 0644)
			fmt.Printf("Таблица сохранена в %s\n", output)
		} else {
			fmt.Printf("Таблица %s -> %s:\n", unitNames[from], unitNames[to])
			for _, row := range rows {
				fmt.Println(row)
			}
		}
		return
	}

	if batch != "" {
		file, _ := os.Open(batch)
		defer file.Close()
		scanner := bufio.NewScanner(file)
		var values []float64
		for scanner.Scan() {
			line := strings.TrimSpace(scanner.Text())
			if line == "" {
				continue
			}
			v, _ := strconv.ParseFloat(line, 64)
			values = append(values, v)
		}
		var results []float64
		for _, v := range values {
			results = append(results, convert(v, from, to))
		}
		if output != "" {
			var lines []string
			for i, v := range values {
				lines = append(lines, fmt.Sprintf("%.*f -> %.*f", precision, v, precision, results[i]))
			}
			os.WriteFile(output, []byte(strings.Join(lines, "\n")), 0644)
			fmt.Printf("Результаты сохранены в %s\n", output)
		} else {
			for i, v := range values {
				fmt.Printf("%.*f -> %.*f\n", precision, v, precision, results[i])
			}
		}
		return
	}

	if flag.NFlag() == 0 {
		fmt.Println("Укажите --value, --batch, --range или --list")
		return
	}

	res := convert(value, from, to)
	fmt.Printf("%.*f %s = %.*f %s\n", precision, value, unitNames[from], precision, res, unitNames[to])
}
