package instantMessenger;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

public class Incoming extends JFrame {

	private JPanel p = new JPanel();
	private JTextField textBox = new JTextField();
	private JTextArea displayBox = new JTextArea();
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private Socket connection;
	private String otherUserIP = "";
	private String otherUserComputerName = "";
	private String user = System.getProperty("user.name");
	private JButton endConnectionButton = new JButton("End Connection");

	public Incoming(Socket c, ObjectInputStream input,
			ObjectOutputStream output) {
		this.output = output;
		this.connection = c;
		setTitle("Incoming Chat With "
				+ connection.getInetAddress().getHostName());

		otherUserIP = connection.getInetAddress().getHostName();
		user = getFirstName(user);

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
				closeConnectionIncoming();
			}
		});

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				closeConnectionIncoming();
			}
		});

		waitForConnection();

		add(p, BorderLayout.NORTH);
		add(new JScrollPane(textBox), BorderLayout.SOUTH);
		add(new JScrollPane(displayBox), BorderLayout.CENTER);
		p.setLayout(new FlowLayout());
		p.add(endConnectionButton);
		setLocation(600, 0);
		setSize(500, 300);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	}

	private void waitForConnection() {
		// TODO Auto-generated method stub
		Thread waitForConnection = new Thread(new Runnable() {

			@Override
			public void run() {
				displayMessage("Connected With " + otherUserIP);
				setupStreamsIncoming();
			}

		});
		waitForConnection.start();
	}

	private String getFirstName(String line) {
		// TODO Auto-generated method stub
		line = line.trim();
		int space = line.indexOf(' ');
		if (space != -1)
			line = line.substring(0, space);
		return line;
	}

	private void closeConnectionIncoming() {
		// TODO Auto-generated method stub
		Thread close = new Thread(new Runnable() {
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
		close.start();
		Client.addresses.remove(otherUserIP);
	}

	private void startChattingIncoming() {// read in what
											// the other
											// person is
											// outputting
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
					closeConnectionIncoming();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					displayMessage("Connection Has Been Disconnected\n");
					textBox.setEditable(false);
					endConnectionButton.setEnabled(false);
					closeConnectionIncoming();
				}
			}
		});
		chatting.start();
	}

	private void setupStreamsIncoming() {
		// TODO Auto-generated method stub
		Thread setup = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					output = new ObjectOutputStream(
							connection.getOutputStream());
					output.flush();
					displayMessage("Waiting For " + otherUserIP + " To Respond");
					input = new ObjectInputStream(connection.getInputStream());
					try {
						otherUserComputerName = (String) input.readObject();
						output.writeObject(user);
						otherUserComputerName = getFirstName(otherUserComputerName);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					displayMessage("Connected To " + otherUserComputerName
							+ " , Start Chatting");
					setTitle("Chatting With " + otherUserComputerName);
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							endConnectionButton.setEnabled(true);
						}

					});
					startChattingIncoming();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
		setup.start();

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
			displayMessage(user + ": " + message);
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
