import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;

public class MainGui {
    private JPanel panel1;
    private JButton jButton;
    private JTextField jTextField;
    private static final String p = "Preparing: ";
    private static final String ps = "Parameters: ";

    public MainGui() {
        jButton.addActionListener(e -> {
            if (jButton == e.getSource()) {
                jTextField.setText("����ע������");
                doSomething();
            }
        });
    }

    private void doSomething() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable trans = clipboard.getContents(null);
        if (trans != null && trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String clipboardStr = (String) trans.getTransferData(DataFlavor.stringFlavor);
                String[] lines = clipboardStr.split("\n");
                String[] psql = null;
                String[] parameters = null;
                for (String line : lines) {
                    if (line.contains(p)) {
                        psql = line.split(p)[line.split(p).length - 1].split("\\?");
                    } else if (line.contains(ps)) {
                        parameters = line.split(ps)[line.split(ps).length - 1].split("\\), ");
                    }
                }
                String returnValue = completeSQL(psql, parameters);
                clipboard.setContents(new StringSelection(returnValue), null);
                jTextField.setText("ƴ���ˣ��ż�������ֱ��ճ������");
            } catch (NullPointerException e){
                jTextField.setText("��û���ƶ�����");
            } catch (Exception e) {
                jTextField.setText(e.getMessage());
            }
        }
    }

    private String completeSQL(String[] psql, String[] parameters) throws Exception {
        if (psql.length != parameters.length + 1) {
            if (psql.length == 0 || parameters.length == 0) {
                throw new Exception("��û���ƶ�����");
            }
            jTextField.setText("��ֳ��ȳ���,SQL����:" + psql.length + "��������:" + parameters.length);
            String parameter;
            for (int i = 0; i < parameters.length; i++) {
                parameter = parameters[i];
                if (parameter != null && parameter.contains("null")) {
                    String[] temp = parameter.split("null, ");
                    //TODO :null�Ĵ���

                    completeSQL(psql, parameters);
                    break;
                }
            }
            throw new Exception("��ֳ���");
        } else {
            StringBuilder finalSql = new StringBuilder(psql[0]);
            String parameter;
            for (int i = 0; i < parameters.length; i++) {
                parameter = parameters[i].substring(0, parameters[i].indexOf('('));
                if (parameters[i].substring(parameters[i].indexOf('(')).contains("String")) {
                    parameter = '"' + parameter + '"';
                }
                finalSql.append(parameter).append(psql[i + 1]);
            }
            return finalSql.toString().trim() + ';';
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainGui");
        frame.setMinimumSize(new Dimension(400, 240));
        frame.setContentPane(new MainGui().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }
}
