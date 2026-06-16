<?php
// length_convert.php - Конвертер длин на PHP (CLI + веб)
// CLI: php length_convert.php --value=100 --from=m --to=km

$units = [
    'mm' => 0.001, 'cm' => 0.01, 'dm' => 0.1, 'm' => 1.0, 'km' => 1000.0,
    'in' => 0.0254, 'ft' => 0.3048, 'yd' => 0.9144,
    'mi' => 1609.344, 'nmi' => 1852.0
];
$unitNames = [
    'mm' => 'Миллиметр', 'cm' => 'Сантиметр', 'dm' => 'Дециметр',
    'm' => 'Метр', 'km' => 'Километр',
    'in' => 'Дюйм', 'ft' => 'Фут', 'yd' => 'Ярд',
    'mi' => 'Миля', 'nmi' => 'Морская миля'
];

function convertLength($value, $from, $to) {
    global $units;
    if ($from == $to) return $value;
    $meters = $value * $units[$from];
    return $meters / $units[$to];
}

function formatUnit($value, $unit, $precision = 2) {
    global $unitNames;
    return number_format($value, $precision, '.', '') . ' ' . $unitNames[$unit];
}

function generateTable($start, $end, $step, $from, $to, $precision = 2) {
    $rows = [];
    for ($v = $start; $v <= $end + 1e-9; $v += $step) {
        $res = convertLength($v, $from, $to);
        $rows[] = [formatUnit($v, $from, $precision), formatUnit($res, $to, $precision)];
    }
    return $rows;
}

// ========== CLI ==========
if (php_sapi_name() === 'cli') {
    $options = getopt("", ["value:", "from:", "to:", "precision:", "batch:", "output:", "range:", "list"]);
    if (isset($options['list'])) {
        echo "Доступные единицы:\n";
        foreach ($unitNames as $code => $name) echo "  $code: $name\n";
        exit;
    }
    $from = $options['from'] ?? 'm';
    $to = $options['to'] ?? 'km';
    $precision = isset($options['precision']) ? (int)$options['precision'] : 2;
    if (isset($options['range'])) {
        list($start, $end, $step) = explode(',', $options['range']);
        $step = (float)$step;
        if ($step <= 0) { echo "Шаг должен быть положительным\n"; exit(1); }
        $table = generateTable((float)$start, (float)$end, $step, $from, $to, $precision);
        if (isset($options['output'])) {
            file_put_contents($options['output'], implode("\n", array_map(function($r) { return $r[0] . ' = ' . $r[1]; }, $table)));
            echo "Таблица сохранена в {$options['output']}\n";
        } else {
            echo "Таблица {$unitNames[$from]} -> {$unitNames[$to]}:\n";
            foreach ($table as $r) echo "{$r[0]} = {$r[1]}\n";
        }
        exit;
    }
    if (isset($options['batch'])) {
        $values = array_filter(array_map('trim', file($options['batch'])), function($l) { return $l !== ''; });
        $results = array_map(function($v) use ($from, $to) { return convertLength((float)$v, $from, $to); }, $values);
        if (isset($options['output'])) {
            $lines = array_map(function($v, $r) use ($precision) {
                return number_format($v, $precision, '.', '') . ' -> ' . number_format($r, $precision, '.', '');
            }, $values, $results);
            file_put_contents($options['output'], implode("\n", $lines));
            echo "Результаты сохранены в {$options['output']}\n";
        } else {
            foreach ($values as $i => $v) echo number_format($v, $precision, '.', '') . ' -> ' . number_format($results[$i], $precision, '.', '') . "\n";
        }
        exit;
    }
    if (isset($options['value'])) {
        $value = (float)$options['value'];
        $res = convertLength($value, $from, $to);
        echo formatUnit($value, $from, $precision) . ' = ' . formatUnit($res, $to, $precision) . "\n";
    } else {
        echo "Укажите --value, --batch, --range или --list\n";
    }
    exit;
}

// ========== ВЕБ-ИНТЕРФЕЙС ==========
?>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Конвертер длин (PHP)</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #f4f7fb; margin: 40px; }
        .container { max-width: 500px; margin: 0 auto; background: white; padding: 20px; border-radius: 16px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        label { display: inline-block; width: 80px; }
        input, select, button { margin: 8px 0; padding: 6px; }
        button { background: #3498db; color: white; border: none; padding: 8px 20px; border-radius: 4px; cursor: pointer; }
        .result { background: #e8f5e9; padding: 10px; border-radius: 8px; margin-top: 10px; }
    </style>
</head>
<body>
<div class="container">
    <h1>📏 Конвертер длин</h1>
    <form method="GET">
        <label>Значение:</label>
        <input type="number" step="any" name="value" value="<?= isset($_GET['value']) ? htmlspecialchars($_GET['value']) : '' ?>" required><br>
        <label>Из:</label>
        <select name="from">
            <?php foreach ($unitNames as $code => $name): ?>
                <option value="<?= $code ?>" <?= isset($_GET['from']) && $_GET['from']==$code ? 'selected' : '' ?>><?= $name ?></option>
            <?php endforeach; ?>
        </select><br>
        <label>В:</label>
        <select name="to">
            <?php foreach ($unitNames as $code => $name): ?>
                <option value="<?= $code ?>" <?= isset($_GET['to']) && $_GET['to']==$code ? 'selected' : '' ?>><?= $name ?></option>
            <?php endforeach; ?>
        </select><br>
        <label>Точность:</label>
        <input type="number" name="precision" value="<?= isset($_GET['precision']) ? $_GET['precision'] : 2 ?>" min="0" max="10"><br>
        <button type="submit">Конвертировать</button>
    </form>
    <?php if (isset($_GET['value']) && is_numeric($_GET['value'])): 
        $value = (float)$_GET['value'];
        $from = $_GET['from'] ?? 'm';
        $to = $_GET['to'] ?? 'km';
        $prec = (int)($_GET['precision'] ?? 2);
        $res = convertLength($value, $from, $to);
    ?>
        <div class="result">
            <strong><?= formatUnit($value, $from, $prec) ?></strong> = <strong><?= formatUnit($res, $to, $prec) ?></strong>
        </div>
    <?php endif; ?>
    <p><small>Поддерживаются: мм, см, дм, м, км, дюймы, футы, ярды, мили, морские мили.</small></p>
</div>
</body>
</html>
