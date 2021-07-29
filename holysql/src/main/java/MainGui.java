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
                jTextField.setText("����ע������");
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
                text = "ƴ���ˣ��ż�������ֱ��ճ������";
                times.set(0);
            } catch (NullPointerException e) {
                handleException("��û���ƶ�����");
            } catch (ArrayIndexOutOfBoundsException e) {
                handleException("���ٳ���㸴�Ƶ��벻�룿");
            } catch (Exception e) {
                handleException(e.getMessage());
            } finally {
                jTextField.setText(text);
            }
        }
    }

    private void handleException(String msg) {
        if (times.incrementAndGet() > 5) {
            text = "�ƣ��ⶼ�����������ˣ��ܲ����а�����sql�������ҿ���";
            times.set(0);
        } else {
            text = msg;
        }
    }

    private String completeSQL(String[] psql, String[] parameters) throws Exception {
        if (psql.length != parameters.length + 1) {
            if (psql.length == 0 || parameters.length == 0) {
                throw new Exception("��û���ƶ�����");
            }
            jTextField.setText("��ֳ��ȳ���,SQL����:" + psql.length + "��������:" + parameters.length);
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
            throw new Exception("��ֳ����ˣ��ǲ��ǲ���û�����룿");
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
                throw new Exception("������ٸ����˵㶫����");
            }
            return finalSql.toString().trim() + ';';
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("��ʥsqlƴ����");
        frame.setMinimumSize(new Dimension(400, 240));
        frame.setContentPane(new MainGui().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }
}
