import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class S25207Project3 extends JFrame {

    private JPanel sendingPanel;
    private JPanel receivingPanel;
    private JPanel middlePanel;

    private int deviceCounter = 0;
    private int btsStationStartCounter = 0;
    private int bscStationCounter = 0;
    private int btsStationEndCounter = 0;

    private BlockingQueue<String> messageQueue;
    private java.util.List<BtsStationStart> btsStationsStart;
    private List<BtsStationEnd> btsStationsEnd;
    private java.util.List<BscStation> bscStations;

    public S25207Project3() {

        setTitle("GSM Network Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        sendingPanel = new JPanel();
        receivingPanel = new JPanel();
        middlePanel = new JPanel();

        sendingPanel.setLayout(new BorderLayout());
        receivingPanel.setLayout(new BorderLayout());
        middlePanel.setLayout(new BorderLayout());
        messageQueue = new LinkedBlockingQueue<>();
        btsStationsStart = new ArrayList<>();
        btsStationsEnd = new ArrayList<>();
        bscStations = new ArrayList<>();

        add(sendingPanel, BorderLayout.WEST);
        add(receivingPanel, BorderLayout.EAST);
        add(middlePanel, BorderLayout.CENTER);

        createSendingDevicesPanel();
        createReceivingDevicesPanel();
        createMiddlePanel();

        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }
    private String selectRandomRecipient() {
        int numReceivingDevices = btsStationsEnd.size();
        if (numReceivingDevices > 0) {
            int randomIndex = new Random().nextInt(numReceivingDevices);
            return "VRD " + (randomIndex + 1);
        }
        return "";
    }

    private String encodeSMS(String recipient, String message) {
        return recipient + ":" + message;
    }

    private int getBtsStationIndexWithLeastWaitingSMS() {
        int minWaitingSMSCount = Integer.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < btsStationsStart.size(); i++) {
            BtsStationStart btsStationStart = btsStationsStart.get(i);
            int waitingSMSCount = btsStationStart.getWaitingSMSCount();
            if (waitingSMSCount < minWaitingSMSCount) {
                minWaitingSMSCount = waitingSMSCount;
                index = i;
            }
        }
        return index;
    }

    private void createSendingDevicesPanel() {
        JPanel sendingDevicesPanel = new JPanel();
        sendingDevicesPanel.setLayout(new BoxLayout(sendingDevicesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(sendingDevicesPanel);

        JButton addButton = new JButton("Add VBD");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = JOptionPane.showInputDialog("Enter a message");
                if (message != null && !message.isEmpty()) {
                    createVirtualSendingDevice(message, sendingDevicesPanel);
                }
            }
          }
        );

        sendingPanel.add(scrollPane, BorderLayout.CENTER);
        sendingPanel.add(addButton, BorderLayout.SOUTH);
    }

    private void createVirtualSendingDevice(String message, JPanel sendingDevicesPanel) {
        JPanel virtualSendingDevicePanel = new JPanel();
        virtualSendingDevicePanel.setLayout(new FlowLayout());

        JLabel messageLabel = new JLabel(message);
        JSlider frequencySlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
        JButton terminateButton = new JButton("Terminate");
        JTextField deviceNumberField = new JTextField();
        deviceNumberField.setEditable(false);
        deviceCounter++;
        deviceNumberField.setText("Device " + deviceCounter);

        JComboBox<String> stateComboBox = new JComboBox<>(new String[]{"WAITING", "ACTIVE"});

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String recipient = selectRandomRecipient();
                String sms = encodeSMS(recipient, message);
                int btsIndex = getBtsStationIndexWithLeastWaitingSMS();
                BtsStationStart btsStationStart = btsStationsStart.get(btsIndex);
                btsStationStart.sendMessage(sms);
             }
           }
        );

        terminateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendingDevicesPanel.remove(virtualSendingDevicePanel);
                sendingDevicesPanel.revalidate();
                sendingDevicesPanel.repaint();
            }
          }
        );

        virtualSendingDevicePanel.add(sendButton);
        virtualSendingDevicePanel.add(messageLabel);
        virtualSendingDevicePanel.add(frequencySlider);
        virtualSendingDevicePanel.add(terminateButton);
        virtualSendingDevicePanel.add(deviceNumberField);
        virtualSendingDevicePanel.add(stateComboBox);

        sendingDevicesPanel.add(virtualSendingDevicePanel);
        sendingDevicesPanel.revalidate();
        sendingDevicesPanel.repaint();
    }

    private void createReceivingDevicesPanel() {
        JPanel receivingDevicesPanel = new JPanel();
        receivingDevicesPanel.setLayout(new BoxLayout(receivingDevicesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(receivingDevicesPanel);

        JButton addButton = new JButton("Add VRD");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createVirtualReceivingDevice(receivingDevicesPanel);
            }
          }
        );

        receivingPanel.add(scrollPane, BorderLayout.CENTER);
        receivingPanel.add(addButton, BorderLayout.SOUTH);
    }

    private void createVirtualReceivingDevice(JPanel receivingDevicesPanel) {
        JPanel virtualReceivingDevicePanel = new JPanel();
        virtualReceivingDevicePanel.setLayout(new FlowLayout());

        JButton removeButton = new JButton("Remove Device");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receivingDevicesPanel.remove(virtualReceivingDevicePanel);
                receivingDevicesPanel.revalidate();
                receivingDevicesPanel.repaint();
            }
          }
        );

        JLabel messageLabel = new JLabel("Received Messages: 0");
        JCheckBox autoRemoveCheckBox = new JCheckBox("Auto Remove");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receivingDevicesPanel.remove(virtualReceivingDevicePanel);
                receivingDevicesPanel.revalidate();
                receivingDevicesPanel.repaint();
            }
          }
        );

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (autoRemoveCheckBox.isSelected()) {
                    SwingUtilities.invokeLater(() -> {
                        messageLabel.setText("Received Messages: 0");
                    });
                }
            }
        }
        ).start();

        virtualReceivingDevicePanel.add(removeButton);
        virtualReceivingDevicePanel.add(messageLabel);
        virtualReceivingDevicePanel.add(autoRemoveCheckBox);

        receivingDevicesPanel.add(virtualReceivingDevicePanel);
        receivingDevicesPanel.revalidate();
        receivingDevicesPanel.repaint();
    }

    private void createMiddlePanel() {
        JPanel btsPanel1 = new JPanel();
        btsPanel1.setLayout(new BoxLayout(btsPanel1, BoxLayout.Y_AXIS));

        JPanel bscPanel = new JPanel();
        bscPanel.setLayout(new BoxLayout(bscPanel, BoxLayout.Y_AXIS));

        JPanel btsPanel2 = new JPanel();
        btsPanel2.setLayout(new BoxLayout(btsPanel2, BoxLayout.Y_AXIS));

        JPanel stationsPanel = new JPanel();
        stationsPanel.setLayout(new GridLayout(1, 3));

        stationsPanel.add(new JScrollPane(btsPanel1));
        stationsPanel.add(new JScrollPane(bscPanel));
        stationsPanel.add(new JScrollPane(btsPanel2));

        middlePanel.add(stationsPanel, BorderLayout.CENTER);

        JButton addBscButton = new JButton("Add BSC");
        addBscButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createBscStation(bscPanel);
            }
          }
        );

        JButton addBtsButton1 = new JButton("Add BTS");
        addBtsButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createBtsStationStart(btsPanel1, getNextBtsStationStartNumber());
            }
          }
        );

        JButton addBtsButton2 = new JButton("Add BTS");
        addBtsButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BTSStationEnd(btsPanel2, getNextBtsStationEndNumber());
            }
        }
        );

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(addBtsButton1);
        buttonPanel.add(addBscButton);
        buttonPanel.add(addBtsButton2);

        middlePanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createBtsStationStart(JPanel btsPanel, int stationNumber) {
        JPanel btsStationStartPanel = new JPanel();
        btsStationStartPanel.setLayout(new BoxLayout(btsStationStartPanel, BoxLayout.Y_AXIS));

        JLabel stationNumberLabel = new JLabel("BTS Station " + stationNumber);

        JButton terminateButton = new JButton("Terminate");
        terminateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btsPanel.remove(btsStationStartPanel);
                btsPanel.revalidate();
                btsPanel.repaint();
            }
          }
        );

        btsStationStartPanel.add(stationNumberLabel);
        btsStationStartPanel.add(terminateButton);

        btsPanel.add(btsStationStartPanel);
        btsPanel.revalidate();
        btsPanel.repaint();
        btsStationsStart.add(new BtsStationStart(stationNumber));
    }
    private void BTSStationEnd(JPanel btsPanel, int stationNumber) {
        JPanel btsStationEndPanel = new JPanel();
        btsStationEndPanel.setLayout(new BoxLayout(btsStationEndPanel, BoxLayout.Y_AXIS));

        JLabel stationNumberLabel = new JLabel("BTS Station " + stationNumber);

        JButton terminateButton = new JButton("Terminate");
        terminateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btsPanel.remove(btsStationEndPanel);
                btsPanel.revalidate();
                btsPanel.repaint();
            }
          }
        );

        btsStationEndPanel.add(stationNumberLabel);
        btsStationEndPanel.add(terminateButton);

        btsPanel.add(btsStationEndPanel);
        btsPanel.revalidate();
        btsPanel.repaint();
        btsStationsEnd.add(new BtsStationEnd(stationNumber));
    }

    private void createBscStation(JPanel bscPanel) {
        JPanel bscStationPanel = new JPanel();
        bscStationPanel.setLayout(new BoxLayout(bscStationPanel, BoxLayout.Y_AXIS));

        JLabel stationNumberLabel = new JLabel("BSC Station " + getNextBscStationNumber());

        JButton terminateButton = new JButton("Terminate");

        terminateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bscPanel.remove(bscStationPanel);
                bscPanel.revalidate();
                bscPanel.repaint();
            }
          }
        );

        bscStationPanel.add(stationNumberLabel);
        bscStationPanel.add(terminateButton);

        bscPanel.add(bscStationPanel);
        bscPanel.revalidate();
        bscPanel.repaint();
        bscStations.add(new BscStation());
    }

    private int getNextBtsStationStartNumber() {
        return ++btsStationStartCounter;
    }

    private int getNextBscStationNumber() {
        return ++bscStationCounter;
    }

    private int getNextBtsStationEndNumber() {
        return ++btsStationEndCounter;
    }

    private class BtsStationStart {
        private int stationNumber;
        private int waitingSMSCount;

        public BtsStationStart(int stationNumber) {
            this.stationNumber = stationNumber;
            this.waitingSMSCount = 0;
        }

        public void sendMessage(String sms) {
            System.out.println("BTS Station Start " + stationNumber + " transmitting SMS: " + sms);
            waitingSMSCount++;
        }
        public int getWaitingSMSCount() {

            return waitingSMSCount;
           }
        }

    private class BtsStationEnd {
        private int stationNumber;
        public BtsStationEnd(int stationNumber) {
            this.stationNumber = stationNumber;
        }
    }

    private class BscStation {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new S25207Project3();
            }
          }
       );
    }
}


