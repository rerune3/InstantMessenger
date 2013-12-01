package server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.TreeMap;

import javax.swing.*;

public class MasterServer extends JFrame {

	private JPanel p = new JPanel();
	private JTextField textBox = new JTextField();
	private JTextArea displayBox = new JTextArea();
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket serverSocket;
	private Socket connection;
	private JButton startServer = new JButton("Start Server");
	private JButton stopServer = new JButton("Stop Server");
	private TreeMap<String, Integer> portInfo = new TreeMap<String, Integer>();

	public MasterServer() {
		super("Server");

		textBox.setEditable(false);
		displayBox.setEditable(false);

		textBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendMessage(e.getActionCommand());
				textBox.setText("");
			}
		});

		startServer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				changeStartButton();
			}

			private void changeStartButton() {
				// TODO Auto-generated method stub
				Thread update = new Thread(new Runnable() {
					public void run() {
						startServer.setEnabled(false);
						stopServer.setEnabled(true);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								try {
									serverSocket = new ServerSocket(6789, 100);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								waitForConnection();
							}

						});
					}
				});
				update.start();
			}
		});

		stopServer.setEnabled(false);
		stopServer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				changeStopButton();
			}

			private void changeStopButton() {
				// TODO Auto-generated method stub
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						startServer.setEnabled(true);
						stopServer.setEnabled(false);
						endConnection();
					}
				});
			}
		});

		add(p, BorderLayout.NORTH);
		add(new JScrollPane(textBox), BorderLayout.SOUTH);
		add(new JScrollPane(displayBox), BorderLayout.CENTER);
		p.setLayout(new FlowLayout());
		p.add(startServer);
		p.add(stopServer);
		setSize(400, 500);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void waitForConnection() {
		// TODO Auto-generated method stub
		Thread waitForConnection = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					while (true) {
						displayMessage("Waiting For Connection...");
						ableToType(false);
						connection = serverSocket.accept();
						displayMessage("Connection Has Been Established With "
								+ connection.getInetAddress().getHostName());
						displayMessage("Waiting For Client To Respond...");
						ableToType(false);// //%$%$%$%$%$%$%$
						setupStreams();
					}
				} catch (SocketException s) {
					displayMessage("Server is Offline");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		waitForConnection.start();
	}

	private void endConnection() {
		try {
			if (input != null)
				input.close();
			if (output != null)
				output.close();
			if (connection != null)
				connection.close();
			if (serverSocket != null)
				serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
					input = new ObjectInputStream(connection.getInputStream());
					final String incomingAddress = connection.getInetAddress()
							.getHostAddress();
					displayMessage(incomingAddress + " Responded\n");
					try {
						portInfo.put(incomingAddress, (int) input.readObject());
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Thread handleConnection = new Thread(new Runnable() {
						public void run() {
							ObjectInputStream inputGoing = input;
							ObjectOutputStream outputGoing = output;
							Socket s = connection;
							while (true) {
								try {
									// writeObject and readObject both block
									outputGoing.writeObject((Integer) portInfo
											.get((String) inputGoing
													.readObject()));
									displayMessage("Request Received From "
											+ s.getInetAddress().getHostName()
											+ "\nProcessed...");

									displayMessage("Server Responded...");
								} catch (ClassNotFoundException | IOException e) {
									// TODO Auto-generated catch block
									displayMessage(s.getInetAddress()
											.getHostName() + " is offline");
									portInfo.remove(s.getInetAddress()
											.getHostAddress());
									break;
								}
							}
						}
					});
					handleConnection.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				waitForConnection();
			}

		});
		setup.start();

	}

	private void ableToType(final boolean b) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				textBox.setEnabled(b);
			}

		});
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
			displayMessage("Server: " + message);
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		MasterServer c = new MasterServer();
	}

}
