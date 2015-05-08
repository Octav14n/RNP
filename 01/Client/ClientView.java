import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClientView {

	private JFrame frame;
	private JTextField textField;
	private JButton sendButton;
	private JTextArea textArea;
	private JButton connectButton;
	private JButton disconnectButton;

	public ClientView() {
		setFrame(new JFrame());
		setTextField(new JTextField());
		setSendButton(new JButton());
		setTextArea(new JTextArea());
		setConnectButton(new JButton());
		setDisconnectButton(new JButton());
		standard();
	}

	public void standard() {
		standartFrame();
		standardTextField();
		standardSendButton();
		standardTextArea();
		standardConnectButton();
		standardDisconnectButton();
		verbindeKomponenten();
	}

	private void standartFrame() {
		getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getFrame().setTitle("Client");
		getFrame().setLayout(null);
		getFrame().setSize(800, 600);
		getFrame().setVisible(true);
	}

	private void standardTextField() {
		getTextField().setSize(600, 30);
		getTextField().setLocation(40, 20);
	}

	private void standardSendButton() {
		getSendButton().setText("Send");
		getSendButton().setSize(100, 30);
		getSendButton().setLocation(660, 20);
	}

	private void standardTextArea() {
		getTextArea().setSize(720, 400);
		getTextArea().setLocation(40, 70);
		getTextArea().setEditable(false);
	}

	private void standardConnectButton() {
		getConnectButton().setText("Connect");
		getConnectButton().setSize(700, 40);
		getConnectButton().setLocation(50, 480);
	}

	private void standardDisconnectButton() {
		getDisconnectButton().setText("Disconnect");
		getDisconnectButton().setSize(700, 40);
		getDisconnectButton().setLocation(50, 520);
	}

	private void verbindeKomponenten() {
		getFrame().add(getTextField());
		getFrame().add(getSendButton());
		getFrame().add(getTextArea());
		getFrame().add(getConnectButton());
		getFrame().add(getDisconnectButton());
	}

	public JFrame getFrame() {
		return frame;
	}

	private void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public JTextField getTextField() {
		return textField;
	}

	private void setTextField(JTextField textField) {
		this.textField = textField;
	}

	public JButton getSendButton() {
		return sendButton;
	}

	private void setSendButton(JButton sendButton) {
		this.sendButton = sendButton;
	}

	public JTextArea getTextArea() {
		return textArea;
	}

	private void setTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}

	public JButton getConnectButton() {
		return connectButton;
	}

	private void setConnectButton(JButton connectButton) {
		this.connectButton = connectButton;
	}

	public JButton getDisconnectButton() {
		return disconnectButton;
	}

	private void setDisconnectButton(JButton disconnectButton) {
		this.disconnectButton = disconnectButton;
	}
}
