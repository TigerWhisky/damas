import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class Damas extends JFrame {
    // ... (Existing variables)

    public Damas() {
        // ... (Existing setup code)

        countdownToStart();
        player1Turn = true;
        startGame();
    }

    // ... (Existing methods: getPlayerName, countdownToStart, createBoardPanel, addPiece)

    private void startGame() {
        while (true) {
            String currentPlayer = player1Turn ? player1Name : player2Name;

            String origin = getValidMove("Qual a peça que deseja movimentar", currentPlayer);
            if (origin == null) continue; // Player resigned

            String destination = getValidMove("Qual o destino da peça?", currentPlayer);
            if (destination == null) continue; // Player resigned

            if (isValidMove(origin, destination)) {
                movePiece(origin, destination);
                if (hasCapturedPiece(origin, destination)) {
                    String capturedPiece = getCapturedPiece(origin, destination);
                    pieces.remove(capturedPiece);
                    if (player1Turn) {
                        player2PiecesLost++;
                    } else {
                        player1PiecesLost++;
                    }
                    updateScore();
                }
                showMoveMessage(currentPlayer, origin, destination);

                if (isGameOver()) {
                    endGame(currentPlayer);
                    return;
                }
                player1Turn = !player1Turn;
            }
        }
    }


    private String getValidMove(String message, String playerName) {
        while (true) {
            // Create a custom dialog without a parent component
            JDialog dialog = new JDialog();
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL); // Block other windows
            dialog.setTitle("Move Input");
            JTextField inputField = new JTextField(10);
            JButton okButton = new JButton("OK");
            JButton desistirButton = new JButton("DESISTIR");

            okButton.addActionListener(e -> dialog.setVisible(false));
            desistirButton.addActionListener(e -> {
                dialog.setVisible(false);
                endGame(player1Turn ? player2Name : player1Name);
                System.exit(0);
            });

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel(playerName + ", " + message), BorderLayout.NORTH);
            panel.add(inputField, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            buttonPanel.add(desistirButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.add(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(null); // Center on screen
            dialog.setVisible(true);

            String move = inputField.getText().trim().toUpperCase();
            if (move.isEmpty()) return null; // Handle DESISTIR or closed dialog

            if (move.matches("[A-Ha-h][1-8]")) {
                return move;
            } else {
                JOptionPane.showMessageDialog(this, "Formato de movimento inválido. Tente novamente.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    private boolean isValidMove(String origin, String destination) {
        int originRow = origin.charAt(0) - 'A';
        int originCol = origin.charAt(1) - '1';
        int destRow = destination.charAt(0) - 'A';
        int destCol = destination.charAt(1) - '1';

        JButton originButton = pieces.get(origin);
        if (originButton == null || !isPlayerPiece(origin)) {
            JOptionPane.showMessageDialog(this, "Movimento Ilegal: Peça não encontrada na origem ou não é sua.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if ((destRow + destCol) % 2 == 0) { // Destination must be a black square
            JOptionPane.showMessageDialog(this, "Movimento Impossivel: Destino inválido (casa branca).", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        JButton destButton = board[destRow][destCol];
        if (!destButton.getText().isEmpty()) { // Destination must be empty
            JOptionPane.showMessageDialog(this, "Movimento Impossivel: Destino ocupado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check if move is diagonal and forward (for regular pieces)
        if (player1Turn) { // White player
            if (destRow - originRow != -1 || Math.abs(destCol - originCol) != 1) { // Not forward diagonal
                JOptionPane.showMessageDialog(this, "Movimento Impossivel: Peças brancas só podem mover para frente na diagonal.", "Erro", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else { // Black player
            if (destRow - originRow != 1 || Math.abs(destCol - originCol) != 1) { // Not forward diagonal
                JOptionPane.showMessageDialog(this, "Movimento Impossivel: Peças pretas só podem mover para frente na diagonal.", "Erro", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true; // Valid move (so far - add capture logic later)
    }



    private void movePiece(String origin, String destination) {
        int destRow = destination.charAt(0) - 'A';
        int destCol = destination.charAt(1) - '1';

        JButton originButton = pieces.get(origin);
        JButton destinationButton = board[destRow][destCol]; // Get the button from the board array

        if (originButton != null && destinationButton != null) {
            destinationButton.setText(originButton.getText());
            destinationButton.setForeground(originButton.getForeground());
            destinationButton.setBorder(originButton.getBorder());

            originButton.setText("");
            originButton.setBorder(null);

            pieces.remove(origin);  // Remove from pieces map
            pieces.put(destination, destinationButton); // Add to pieces map with new position

        }
    }


    private void showMoveMessage(String player, String origin, String destination) {
        JOptionPane.showMessageDialog(this, "O jogador " + player + " moveu a peça de " + origin + " para " + destination, "Movimento", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean isGameOver() {
        return pieces.size() < 12;
    }

    private void endGame(String winnerName) {
        JOptionPane.showMessageDialog(this, "O Vencedor foi o jogador " + winnerName + "!");
        System.exit(0);
    }

    private void updateScore() {
        scoreLabel.setText(player1Name + ": " + player1PiecesLost + " vs " + player2Name + ": " + player2PiecesLost);
    }

    private boolean hasCapturedPiece(String origin, String destination) {
        int originRow = origin.charAt(0) - 'A';
        int originCol = origin.charAt(1) - '1';
        int destRow = destination.charAt(0) - 'A';
        int destCol = destination.charAt(1) - '1';
        return Math.abs(destRow - originRow) == 2 && Math.abs(destCol - originCol) == 2;
    }

    private String getCapturedPiece(String origin, String destination) {
        int originRow = origin.charAt(0) - 'A';
        int originCol = origin.charAt(1) - '1';
        int destRow = destination.charAt(0) - 'A';
        int destCol = destination.charAt(1) - '1';
        char capturedRow = (char) ('A' + (originRow + destRow) / 2);
        char capturedCol = (char) ('1' + (originCol + destCol) / 2);
        return String.format("%c%c", capturedRow, capturedCol);
    }



    public static void main(String[] args) {  // <--- Main method is here!
        SwingUtilities.invokeLater(Damas::new);
    }
}
