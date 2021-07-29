import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.concurrent.atomic.AtomicInteger;

public class MainGui {
    private JPanel panel1;
    private JButton jButton;
    private JTextField jTextField;
    private static final String p = "Preparing: ";
    private static final String ps = "Parameters: ";
    private static AtomicInteger times = new AtomicInteger(0);
    private static String text = null;
    private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public MainGui() {
        jButton.addActionListener(e -> {
            if (jButton == e.getSource()) {
                jTextField.setText("我在注视着你");
                doSomething();
            }
        });
    }

    private void doSomething() {
        Transferable trans = clipboard.getContents(null);
        if (trans != null && trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String clipboardStr = (String) trans.getTransferData(DataFlavor.stringFlavor);
                String[] lines = clipboardStr.split("\n");
                String[] psql = null;
                String[] parameters = null;
                for (String line : lines) {
                    if (line.contains(p)) {
                        if (line.endsWith("?")){
                            line = line + " ";
                        }
                        psql = line.split(p)[line.split(p).length - 1].split("\\?");
                    } else if (line.contains(ps)) {
                        parameters = line.split(ps)[line.split(ps).length - 1].split("\\), ");
                    }
                }
                String returnValue = completeSQL(psql, parameters);
                clipboard.setContents(new StringSelection(returnValue), null);
                text = "拼完了，放剪贴板了直接粘贴就行";
                times.set(0);
            } catch (NullPointerException e) {
                handleException("你没复制东西吧");
            } catch (ArrayIndexOutOfBoundsException e) {
                handleException("你再瞅瞅你复制的齐不齐？");
            } catch (Exception e) {
                handleException(e.getMessage());
            } finally {
                jTextField.setText(text);
            }
        }
    }

    private void handleException(String msg) {
        if (times.incrementAndGet() > 5) {
            text = "淦，这都连续错六次了，能不能行啊，把sql发我让我看看";
            times.set(0);
        } else {
            text = msg;
        }
    }

    private String completeSQL(String[] psql, String[] parameters) throws Exception {
        if (psql.length != parameters.length + 1) {
            if (psql.length == 0 || parameters.length == 0) {
                throw new Exception("你没复制东西吧");
            }
            jTextField.setText("拆分长度出错,SQL长度:" + psql.length + "参数长度:" + parameters.length);
            for (String parameter : parameters) {
                if (parameter != null && parameter.contains("null")) {
                    Transferable trans = clipboard.getContents(null);
                    String clipboardStr = (String) trans.getTransferData(DataFlavor.stringFlavor);
                    String[] lines = clipboardStr.split("\n");
                    String[] psqlTmp = null;
                    String[] parametersTmp = null;
                    for (String line : lines) {
                        if (line.contains(p)) {
                            psqlTmp = line.split(p)[line.split(p).length - 1].split("\\?");
                        } else if (line.contains(ps)) {
                            parametersTmp = line.replace("null, ", "null(null), ").split(ps)[line.split(ps).length - 1].split("\\), ");
                        }
                    }
                    return completeSQL(psqlTmp, parametersTmp);
                }
            }
            throw new Exception("拆分出错了，是不是参数没复制齐？");
        } else {
            StringBuilder finalSql = new StringBuilder(psql[0]);
            String parameter;
            try {
                for (int i = 0; i < parameters.length; i++) {
                    if (i == parameters.length - 1 && parameters[i].contains("null")) {
                        parameters[i] = "null(n";
                    }
                    parameter = parameters[i].substring(0, parameters[i].indexOf('('));
                    if (parameters[i].substring(parameters[i].indexOf('(')).contains("String")) {
                        parameter = '"' + parameter + '"';
                    }
                    finalSql.append(parameter).append(psql[i + 1]);
                }
            } catch (StringIndexOutOfBoundsException e) {
                throw new Exception("你最后少复制了点东西吧");
            }
            return finalSql.toString().trim() + ';';
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("神圣sql拼接器");
        frame.setMinimumSize(new Dimension(400, 240));
        frame.setContentPane(new MainGui().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }
}
