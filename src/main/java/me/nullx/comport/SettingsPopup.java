package me.nullx.comport;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jssc.SerialPort;

public class SettingsPopup extends JFrame {

    JComboBox<Integer> baudrate;
    JComboBox<Integer> databits;
    JComboBox<Integer> stopbits;
    JComboBox<String> parity;
    JComboBox<String> flowcontrol;
    JCheckBox sendOnEnter;
    JCheckBox sendAsHex;
    JCheckBox readAsHex;
    JLabel sendAsHexNote;
    JLabel readAsHexNote;

    JLabel about = new JLabel("Made by nullx (nullx.me)");

    JButton submit;

    public SettingsPopup() {
        super("Settings");
        build();
        setSize(300, 300);
    }

    private void build() {
        baudrate = new JComboBox<Integer>();
        databits = new JComboBox<Integer>();
        stopbits = new JComboBox<Integer>();
        parity = new JComboBox<String>();
        flowcontrol = new JComboBox<String>();
        sendOnEnter = new JCheckBox("Send on Enter");
        sendAsHex = new JCheckBox("Send as Hex");
        sendAsHexNote = new JLabel("Note: Each byte is separated by a space (e.g. 5 0xFF 255 1 24)");
        readAsHex = new JCheckBox("Read as Hex");
        readAsHexNote = new JLabel("Note: Each byte will be shown separated by a space (e.g. 5 0xFF 255 1 24)");

        // set font size
        sendAsHexNote.setFont(sendAsHexNote.getFont().deriveFont(10f));
        sendAsHexNote.setVisible(false);

        readAsHexNote.setFont(readAsHexNote.getFont().deriveFont(10f));
        readAsHexNote.setVisible(false);

        about.setFont(about.getFont().deriveFont(10f));

        baudrate.addItem(9600);
        baudrate.addItem(19200);
        baudrate.addItem(38400);
        baudrate.addItem(57600);
        baudrate.addItem(115200);

        databits.addItem(5);
        databits.addItem(6);
        databits.addItem(7);
        databits.addItem(8);
        databits.addItem(9);

        stopbits.addItem(1);
        stopbits.addItem(2);

        parity.addItem("None");
        parity.addItem("Odd");
        parity.addItem("Even");
        parity.addItem("Mark");
        parity.addItem("Space");

        flowcontrol.addItem("None");
        flowcontrol.addItem("Xon/Xoff");
        flowcontrol.addItem("RTS/CTS");

        sendOnEnter.setSelected(true);

        // create a label for each of the components
        JLabel baudrateLbl = new JLabel("Baudrate");
        JLabel databitsLbl = new JLabel("Data Bits");
        JLabel stopbitsLbl = new JLabel("Stop Bits");
        JLabel parityLbl = new JLabel("Parity");
        JLabel flowcontrolLbl = new JLabel("Flow Control");

        // create a panel for each of the components
        JPanel baudratePanel = new JPanel();
        JPanel databitsPanel = new JPanel();
        JPanel stopbitsPanel = new JPanel();
        JPanel parityPanel = new JPanel();
        JPanel flowcontrolPanel = new JPanel();
        JPanel sendAsHexPanel = new JPanel(new java.awt.GridLayout(2, 1));
        JPanel readAsHexPanel = new JPanel(new java.awt.GridLayout(2, 1));

        // create a submit button
        submit = new JButton("Submit");

        // add the components to the panels
        baudratePanel.add(baudrateLbl);
        baudratePanel.add(baudrate);
        databitsPanel.add(databitsLbl);
        databitsPanel.add(databits);
        stopbitsPanel.add(stopbitsLbl);
        stopbitsPanel.add(stopbits);
        parityPanel.add(parityLbl);
        parityPanel.add(parity);
        flowcontrolPanel.add(flowcontrolLbl);
        flowcontrolPanel.add(flowcontrol);
        sendAsHexPanel.add(sendAsHex);
        sendAsHexPanel.add(sendAsHexNote);
        readAsHexPanel.add(readAsHex);
        readAsHexPanel.add(readAsHexNote);

        // add the panels to the popup
        JPanel panel = new JPanel();
        panel.add(baudratePanel);
        panel.add(databitsPanel);
        panel.add(stopbitsPanel);
        panel.add(parityPanel);
        panel.add(flowcontrolPanel);
        panel.add(sendOnEnter);
        panel.add(sendAsHexPanel);
        panel.add(readAsHexPanel);
        panel.add(submit);
        panel.add(about);

        panel.setLayout(new java.awt.GridLayout(panel.getComponentCount(), 1));

        // on submit, close the popup
        submit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setVisible(false);
            }
        });

        sendAsHex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (sendAsHex.isSelected()) {
                    sendAsHexNote.setVisible(true);
                } else {
                    sendAsHexNote.setVisible(false);
                }
            }
        });

        readAsHex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (readAsHex.isSelected()) {
                    readAsHexNote.setVisible(true);
                } else {
                    readAsHexNote.setVisible(false);
                }
            }
        });

        this.add(panel);
    }

    public int getBaudrate() {
        return (int) baudrate.getSelectedItem();
    }

    public int getDatabits() {
        return (int) databits.getSelectedItem();
    }

    public int getStopbits() {
        return (int) stopbits.getSelectedItem();
    }

    public int getParity() {
        switch ((String) parity.getSelectedItem()) {
            case "None":
                return SerialPort.PARITY_NONE;

            case "Odd":
                return SerialPort.PARITY_ODD;

            case "Even":
                return SerialPort.PARITY_EVEN;

            case "Mark":
                return SerialPort.PARITY_MARK;

            default:
                break;
        }

        return SerialPort.PARITY_NONE;
    }

    public int getFlowcontrol() {
        String item = (String) flowcontrol.getSelectedItem();
        
        if (item.equals("None")) {
            return SerialPort.FLOWCONTROL_NONE;
        } else if (item.equals("Xon/Xoff")) {
            return SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT;
        } else if (item.equals("RTS/CTS")) {
            return SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT;
        }

        return SerialPort.FLOWCONTROL_NONE;
    }

    public boolean getSendOnEnter() {
        return sendOnEnter.isSelected();
    }

    public boolean getSendAsHex() {
        return sendAsHex.isSelected();
    }

    public boolean getReadAsHex() {
        return readAsHex.isSelected();
    }

    public void display() {
        this.pack();
        setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
