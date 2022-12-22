package me.nullx.comport;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class MyWindow extends JFrame {
    JLabel statusBarLbl;
    JTextArea rcvTerminalTf = new JTextArea();
    JTextArea sendTerminalTf = new JTextArea();
    JComboBox<String> portList;
    JButton connectButton;
    JButton disconnectButton;
    JButton refreshButton;
    JScrollPane rcvPanelScroll;
    JScrollPane sendPanelScroll;
    JPanel centerPanel;

    static final int MAX_RCV_HISTORY = 10000;

    SettingsPopup settingsPopup = new SettingsPopup();

    SerialPort serialPort;

    public MyWindow(String title) {
        super(title);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        initComponents();
        setSize(800, 500);
        displayConnected(false);
        this.pack();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel topBar = new JPanel();
        portList = new JComboBox<String>();
        refreshButton = new JButton();
        connectButton = new JButton();
        JButton settingsButton = new JButton();
        disconnectButton = new JButton();

        // create a bottom bar that has a text field
        JPanel bottomBar = new JPanel();
        statusBarLbl = new JLabel();

        // create a center panel that has terminal
        centerPanel = new JPanel();
        rcvTerminalTf = new JTextArea(10, 10);
        sendTerminalTf = new JTextArea(10, 10);

        // set the layout of the window
        setLayout(new BorderLayout());

        // set the layout of the top bar
        topBar.setLayout(new FlowLayout());

        // set the layout of the bottom bar
        bottomBar.setLayout(new BorderLayout());

        // set the layout of the center panel
        centerPanel.setLayout(new GridLayout(1, 2));

        // set the text of the buttons
        refreshButton.setText("Refresh");
        connectButton.setText("Connect");
        settingsButton.setText("Settings");
        disconnectButton.setText("Disconnect");

        // set the text of the status bar
        statusBarLbl.setText("-");

        // set the border of the bottom bar
        bottomBar.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        topBar.add(portList);
        topBar.add(refreshButton);
        topBar.add(connectButton);
        topBar.add(disconnectButton);
        topBar.add(settingsButton);

        // add the status bar to the bottom bar
        bottomBar.add(statusBarLbl);

        // add the terminal to the center panel
        JPanel rcvPanel = new JPanel();
        // fill the panel with the terminal
        rcvPanel.setLayout(new BorderLayout());
        rcvPanel.add(new JLabel("Receive"), BorderLayout.NORTH);

        // no touch events allowed
        rcvTerminalTf.setEditable(false);
        rcvPanel.add(new JScrollPane(rcvTerminalTf), BorderLayout.CENTER);

        rcvTerminalTf.setLineWrap(true);
        rcvTerminalTf.setWrapStyleWord(true);

        centerPanel.add(rcvPanel);

        JPanel sendPanel = new JPanel();
        sendPanel.setLayout(new BorderLayout());
        sendPanel.add(new JLabel("Send"), BorderLayout.NORTH);
        sendPanel.add(new JScrollPane(sendTerminalTf), BorderLayout.CENTER);

        centerPanel.add(sendPanel);

        // add the top bar to the top of the window
        add(topBar, BorderLayout.NORTH);

        // add the bottom bar to the bottom of the window
        add(bottomBar, BorderLayout.SOUTH);

        // add the center panel to the center of the window
        add(centerPanel, BorderLayout.CENTER);

        // initialize the port list
        refreshPortList(portList);

        // add the action listener to the refresh button
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshPortList(portList);
            }
        });

        // add action listener to the settings button
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsPopup.display();
            }
        });

        // connect to the port when the connect button is pressed
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boolean succs = connectTo((String) portList.getSelectedItem());
                if (succs) {
                    displayConnected(true);
                    listenChanges();
                }
            }
        });

        // disconnect from the port when the disconnect button is pressed
        disconnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boolean sucss = disconnect();
                if (sucss)
                    displayConnected(false);
            }
        });

        // add the action listener to the send terminal
        sendTerminalTf.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                send();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                send();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                send();
            }

            private void send() {
                if (serialPort == null)
                    return;
                String text = sendTerminalTf.getText();
                if (text.length() == 0)
                    return;
                // check if the last character is a new line
                boolean sendOnEnter = MyWindow.this.settingsPopup.getSendOnEnter();
                if (text.charAt(text.length() - 1) != '\n' && sendOnEnter) {
                    return;
                }

                if (sendOnEnter) {
                    // remove the new line
                    text = text.substring(0, text.length() - 1);
                }

                try {
                    byte[] bytes;
                    if (MyWindow.this.settingsPopup.getSendAsHex()) {
                        // convert the text to hex
                        bytes = HexUtils.hexStringToByteArray(text);
                    } else {
                        // convert the text to bytes
                        bytes = text.getBytes();
                    }
                    // send the text
                    boolean sucss = serialPort.writeBytes(bytes);
                    if (sucss) {
                        MyWindow.this.setStatusBar("Sent " + bytes.length + " bytes");
                    } else {
                        MyWindow.this.setStatusBar("Error sending data");
                    }

                    // clear the text field
                    Runnable doClear = new Runnable() {
                        @Override
                        public void run() {
                            sendTerminalTf.setText("");
                        }
                    };

                    SwingUtilities.invokeLater(doClear);
                } catch (SerialPortException e) {
                    MyWindow.this.setStatusBar("Error sending data: " + e.getMessage());
                }
            }
        });

        // on rcvTerminalTf new text scroll to the bottom
        rcvTerminalTf.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scroll();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scroll();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scroll();
            }

            private void scroll() {
                rcvTerminalTf.setCaretPosition(rcvTerminalTf.getDocument().getLength());
            }
        });
    }

    private void displayConnected(boolean connected) {
        connectButton.setEnabled(!connected);
        disconnectButton.setEnabled(connected);
        portList.setEnabled(!connected);

        refreshButton.setEnabled(!connected);

        rcvTerminalTf.setEnabled(connected);
        sendTerminalTf.setEnabled(connected);

        Color color = connected ? Color.WHITE : Color.LIGHT_GRAY;
        rcvTerminalTf.setBackground(color);
        sendTerminalTf.setBackground(color);

        if (connected) {
            rcvTerminalTf.setText("");
            sendTerminalTf.setText("");
        }
    }

    private void refreshPortList(JComboBox<String> portList) {
        portList.removeAllItems();
        String[] ports = Comunications.getPorts();

        // add the ports to the port list
        for (String port : ports) {
            portList.addItem(port);
        }
    }

    private boolean disconnect() {
        if (serialPort == null)
            return true;
        try {
            Comunications.disconnect(serialPort);
            setStatusBar("Disconnected");
        } catch (SerialPortException e) {
            this.setStatusBar("Error disconnecting: " + e.getMessage());
            return false;
        }

        serialPort = null;
        return true;
    }

    private boolean connectTo(String portName) {
        int baudRate = settingsPopup.getBaudrate();
        int dataBits = settingsPopup.getDatabits();
        int stopBits = settingsPopup.getStopbits();
        int parity = settingsPopup.getParity();
        int flowControl = settingsPopup.getFlowcontrol();

        try {
            serialPort = Comunications.connect(portName, baudRate, dataBits, stopBits, parity, flowControl);
            setStatusBar("Connected to " + portList.getSelectedItem());
        } catch (SerialPortException e) {
            this.setStatusBar("Error connecting: " + e.getMessage());
            return false;
        }

        return true;
    }

    private void listenChanges() {
        if (serialPort == null)
            return;

        // listen for changes
        try {
            serialPort.addEventListener(new SerialPortEventListener() {
                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.isRXCHAR()) {
                        try {
                            byte[] bytes = serialPort.readBytes();
                            String text;
                            if (MyWindow.this.settingsPopup.getReadAsHex()) {
                                text = HexUtils.bytesToHex(bytes);
                            } else {
                                text = new String(bytes);
                            }

                            // cut the text if it is too long
                            String allText = rcvTerminalTf.getText();
                            if (allText.length() + text.length() > MAX_RCV_HISTORY) {
                                // delete overflow first lines
                                int overflow = allText.length() + text.length() - MAX_RCV_HISTORY;
                                allText = allText.substring(overflow);
                                rcvTerminalTf.setText(allText);
                            }

                            rcvTerminalTf.append(text);
                            setStatusBar("Received " + bytes.length + " bytes");
                        } catch (SerialPortException e) {
                            setStatusBar("Error receiving data: " + e.getMessage());
                        }
                    }
                }
            }, SerialPort.MASK_RXCHAR);

        } catch (Exception e) {
            setStatusBar("Error listening for changes: " + e.getMessage());
        }
    }

    private void setStatusBar(String text) {
        statusBarLbl.setText(text);
    }

    // on dispose run a disconnect
    public void dispose() {
        disconnect();
        super.dispose();
    }
}
