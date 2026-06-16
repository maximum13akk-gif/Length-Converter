// LengthConverter.java - Конвертер длин на Java (CLI + Swing GUI)
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class LengthConverter {
    private static final Map<String, Double> UNITS = new LinkedHashMap<>();
    private static final Map<String, String> UNIT_NAMES = new LinkedHashMap<>();
    static {
        UNITS.put("mm", 0.001); UNIT_NAMES.put("mm", "Миллиметр");
        UNITS.put("cm", 0.01);  UNIT_NAMES.put("cm", "Сантиметр");
        UNITS.put("dm", 0.1);   UNIT_NAMES.put("dm", "Дециметр");
        UNITS.put("m", 1.0);    UNIT_NAMES.put("m", "Метр");
        UNITS.put("km", 1000.0); UNIT_NAMES.put("km", "Километр");
        UNITS.put("in", 0.0254); UNIT_NAMES.put("in", "Дюйм");
        UNITS.put("ft", 0.3048); UNIT_NAMES.put("ft", "Фут");
        UNITS.put("yd", 0.9144); UNIT_NAMES.put("yd", "Ярд");
        UNITS.put("mi", 1609.344); UNIT_NAMES.put("mi", "Миля");
        UNITS.put("nmi", 1852.0); UNIT_NAMES.put("nmi", "Морская миля");
    }

    public static double convert(double value, String from, String to) {
        if (from.equals(to)) return value;
        double meters = value * UNITS.get(from);
        return meters / UNITS.get(to);
    }

    public static List<Double> convertBatch(List<Double> values, String from, String to) {
        List<Double> res = new ArrayList<>();
        for (double v : values) res.add(convert(v, from, to));
        return res;
    }

    public static List<String[]> generateTable(double start, double end, double step, String from, String to, int precision) {
        List<String[]> rows = new ArrayList<>();
        for (double v = start; v <= end + 1e-9; v += step) {
            double res = convert(v, from, to);
            rows.add(new String[]{
                String.format("%." + precision + "f %s", v, UNIT_NAMES.get(from)),
                String.format("%." + precision + "f %s", res, UNIT_NAMES.get(to))
            });
        }
        return rows;
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--gui")) {
            SwingUtilities.invokeLater(() -> new LengthConverterGUI().setVisible(true));
            return;
        }
        // CLI (упрощённый)
        Map<String, String> opts = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                if (i + 1 < args.length && !args[i+1].startsWith("--")) {
                    opts.put(args[i], args[++i]);
                } else {
                    opts.put(args[i], "");
                }
            }
        }
        try {
            if (opts.containsKey("--list")) {
                System.out.println("Доступные единицы:");
                for (Map.Entry<String, String> e : UNIT_NAMES.entrySet())
                    System.out.printf("  %s: %s\n", e.getKey(), e.getValue());
                return;
            }
            String from = opts.getOrDefault("--from", "m");
            String to = opts.getOrDefault("--to", "km");
            int precision = Integer.parseInt(opts.getOrDefault("--precision", "2"));
            if (opts.containsKey("--range")) {
                String[] parts = opts.get("--range").split(",");
                if (parts.length != 3) throw new Exception("Формат: start,end,step");
                double start = Double.parseDouble(parts[0]);
                double end = Double.parseDouble(parts[1]);
                double step = Double.parseDouble(parts[2]);
                if (step <= 0) throw new Exception("Шаг должен быть положительным");
                List<String[]> table = generateTable(start, end, step, from, to, precision);
                if (opts.containsKey("--output")) {
                    PrintWriter pw = new PrintWriter(opts.get("--output"));
                    for (String[] row : table) pw.println(row[0] + " = " + row[1]);
                    pw.close();
                    System.out.println("Таблица сохранена в " + opts.get("--output"));
                } else {
                    System.out.println("Таблица " + UNIT_NAMES.get(from) + " -> " + UNIT_NAMES.get(to) + ":");
                    for (String[] row : table) System.out.println(row[0] + " = " + row[1]);
                }
                return;
            }
            if (opts.containsKey("--batch")) {
                List<Double> values = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(opts.get("--batch")))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) values.add(Double.parseDouble(line));
                    }
                }
                List<Double> results = convertBatch(values, from, to);
                if (opts.containsKey("--output")) {
                    PrintWriter pw = new PrintWriter(opts.get("--output"));
                    for (int i = 0; i < values.size(); i++)
                        pw.printf("%." + precision + "f -> %." + precision + "f\n", values.get(i), results.get(i));
                    pw.close();
                    System.out.println("Результаты сохранены в " + opts.get("--output"));
                } else {
                    for (int i = 0; i < values.size(); i++)
                        System.out.printf("%." + precision + "f -> %." + precision + "f\n", values.get(i), results.get(i));
                }
                return;
            }
            if (opts.containsKey("--value")) {
                double value = Double.parseDouble(opts.get("--value"));
                double res = convert(value, from, to);
                System.out.printf("%." + precision + "f %s = %." + precision + "f %s\n", value, UNIT_NAMES.get(from), res, UNIT_NAMES.get(to));
            } else {
                System.out.println("Укажите --value, --batch, --range, --list или --gui");
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    // ========== GUI ==========
    static class LengthConverterGUI extends JFrame {
        private JTextField valueField, resultField;
        private JComboBox<String> fromBox, toBox;
        private JSpinner precisionSpinner;

        public LengthConverterGUI() {
            setTitle("Конвертер длин");
            setSize(450, 300);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0; gbc.gridy = 0;
            add(new JLabel("Значение:"), gbc);
            gbc.gridx = 1;
            valueField = new JTextField(10);
            add(valueField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            add(new JLabel("Из:"), gbc);
            gbc.gridx = 1;
            fromBox = new JComboBox<>(UNITS.keySet().toArray(new String[0]));
            fromBox.setSelectedItem("m");
            add(fromBox, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            add(new JLabel("В:"), gbc);
            gbc.gridx = 1;
            toBox = new JComboBox<>(UNITS.keySet().toArray(new String[0]));
            toBox.setSelectedItem("km");
            add(toBox, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            add(new JLabel("Точность:"), gbc);
            gbc.gridx = 1;
            precisionSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 10, 1));
            add(precisionSpinner, gbc);

            JButton convertBtn = new JButton("Конвертировать");
            convertBtn.addActionListener(e -> convert());
            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
            add(convertBtn, gbc);

            gbc.gridy = 5;
            add(new JLabel("Результат:"), gbc);
            gbc.gridx = 1;
            resultField = new JTextField(15);
            resultField.setEditable(false);
            add(resultField, gbc);

            JPanel btnPanel = new JPanel(new FlowLayout());
            JButton reverseBtn = new JButton("Обратный");
            reverseBtn.addActionListener(e -> reverse());
            btnPanel.add(reverseBtn);
            JButton tableBtn = new JButton("Таблица");
            tableBtn.addActionListener(e -> showTable());
            btnPanel.add(tableBtn);
            gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2;
            add(btnPanel, gbc);
        }

        private void convert() {
            try {
                double val = Double.parseDouble(valueField.getText());
                String from = (String) fromBox.getSelectedItem();
                String to = (String) toBox.getSelectedItem();
                int prec = (Integer) precisionSpinner.getValue();
                double res = LengthConverter.convert(val, from, to);
                resultField.setText(String.format("%." + prec + "f %s", res, UNIT_NAMES.get(to)));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
            }
        }

        private void reverse() {
            Object from = fromBox.getSelectedItem();
            Object to = toBox.getSelectedItem();
            fromBox.setSelectedItem(to);
            toBox.setSelectedItem(from);
            if (!resultField.getText().isEmpty()) convert();
        }

        private void showTable() {
            try {
                double val = Double.parseDouble(valueField.getText());
                double start = val - 10, end = val + 10, step = 1.0;
                int prec = (Integer) precisionSpinner.getValue();
                String from = (String) fromBox.getSelectedItem();
                String to = (String) toBox.getSelectedItem();
                List<String[]> table = LengthConverter.generateTable(start, end, step, from, to, prec);
                StringBuilder sb = new StringBuilder();
                sb.append(UNIT_NAMES.get(from)).append(" -> ").append(UNIT_NAMES.get(to)).append("\n");
                for (String[] row : table) sb.append(row[0]).append(" = ").append(row[1]).append("\n");
                JTextArea area = new JTextArea(sb.toString(), 15, 40);
                area.setEditable(false);
                JOptionPane.showMessageDialog(this, new JScrollPane(area), "Таблица", JOptionPane.PLAIN_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
            }
        }
    }
}
