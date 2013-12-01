package instantMessenger;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

public class Outgoing extends JFrame {

	private JPanel p = new JPanel();
	private JTextField textBox = new JTextField();
	private JTextArea displayBox = new JTextArea();
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private Socket connection;
	private String otherUserIP = "";
	private String user = System.getProperty("user.name");
	private String otherUserComputerName = "";
	private int port = 0;
	private JButton endConnectionButton = new JButton("End Connection");

	public Outgoing(String ip, int pt) {
		super("Outgoing");
		otherUserIP = ip;
		this.port = pt;
		getFirstName(otherUserComputerName);
		endConnectionButton.setEnabled(false);
		textBox.setEditable(false);
		displayBox.setEditable(false);
		displayBox.setText("Welcome!\n");

		textBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (otherUserIP != null)
					sendMessage(e.getActionCommand());
				textBox.setText("");
			}
		});

		endConnectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeConnection();
			}
		});
		
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				closeConnection();
			}
		});

		establishConnection();

		add(p, BorderLayout.NORTH);
		add(new JScrollPane(textBox), BorderLayout.SOUTH);
		add(new JScrollPane(displayBox), BorderLayout.CENTER);
		p.setLayout(new FlowLayout());
		p.add(endConnectionButton);
		setLocation(300, 0);
		setSize(500, 300);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	}

	private void establishConnection() {
		Thread establish = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				displayMessage("Attempting To Connect to..." + otherUserIP);
				try {
					connection = new Socket(otherUserIP, port);
				} catch (UnknownHostException e) {
					displayMessage("Invalid IP Address");
					return;
				} catch (ConnectException e) {
					displayMessage("Connection Timed Out");
					return;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				endConnectionButton.setEnabled(true);
				setTitle("Chat With "
						+ connection.getInetAddress().getHostName());
				displayMessage("Connected With "
						+ connection.getInetAddress().getHostName());
				setupStreams();
			}
		});
		establish.start();
	}

	private void closeConnection() {
		// TODO Auto-generated method stub
		Thread chatting = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					input.close();
					output.close();
					connection.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		chatting.start();
		Client.addresses.remove(otherUserIP);
	}

	private void startChatting() {// read in what the other person is outputting
		// TODO Auto-generated method stub
		Thread chatting = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String message = null;
				textBox.setEditable(true);
				try {
					while (!connection.isInputShutdown()) {
						try {
							message = (String) input.readObject();
							displayMessage(otherUserComputerName + ": " + message);
						} catch (ClassNotFoundException e) {
							displayMessage("Invalid Entry\n");
						}
					}
					displayMessage("Connection Has Been Disconnected\n");
					textBox.setEditable(false);
					endConnectionButton.setEnabled(false);
					closeConnection();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					displayMessage("Connection Has Been Disconnected\n");
					textBox.setEditable(false);
					endConnectionButton.setEnabled(false);
					closeConnection();
				}
			}
		});
		chatting.start();
	}

	private void setupStreams() {
		// TODO Auto-generated method stub
		Thread setup = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					output = new ObjectOutputStream(connection
							.getOutputStream());
					output.flush();
					displayMessage("Waiting For "
							+ connection.getInetAddress().getHostName()
							+ " To Respond");
					input = new ObjectInputStream(connection.getInputStream());
					try {
						output.writeObject(user);
						otherUserComputerName = (String) input.readObject();
						otherUserComputerName = getFirstName(otherUserComputerName);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					displayMessage(otherUserComputerName + " is Responsive, Start Chatting");
					setTitle("Chatting With " + otherUserComputerName);
					startChatting();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
		setup.start();

	}

	private String getFirstName(String line) {
		// TODO Auto-generated method stub
		line.trim();
		int space = line.indexOf(' ');
		if (space != -1)
			line = line.substring(0, space);
		return line;
	}

	private void displayMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				displayBox.append(message + "\n");
			}
		});
	}

	private void sendMessage(String message) {
		try {
			output.writeObject(message);
			int space = user.indexOf(' ');
			if (space != -1)
				user = user.substring(0, space);
			displayMessage(user + ": " + message);
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
