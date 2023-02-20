package com.chessanalysis.util;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.File;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

public class EnPassantDetector {

    private EnPassantDetector() {
        // Empty constructor
    }

    private static boolean checkSingleSquare(Board board, Piece target, int idx) {
        Piece piece = board.getPiece(Square.squareAt(idx));
        return (piece.getPieceType() == PieceType.PAWN
                && piece.getPieceSide() != target.getPieceSide());
    }

    private static boolean checkSquareDeclined(Board board, File file) {
        Piece target = board.getPiece(board.getEnPassantTarget());
        int idx = board.getEnPassantTarget().ordinal();
        if (file != File.FILE_A && checkSingleSquare(board, target, idx-1)) {
            return true;
        }
        return (file != File.FILE_H && checkSingleSquare(board, target, idx+1));
    }

    public static MoveType isEnPassant(Board board, Move move) {
        if (board.getEnPassantTarget() != Square.NONE) {
            Piece movePiece = board.getPiece(move.getFrom());
            if (move.getTo() == board.getEnPassant()
                    && movePiece.getPieceType() == PieceType.PAWN) {
                return MoveType.EN_PASSANT_ACCEPTED;
            } else {
                File file = board.getEnPassantTarget().getFile();
                if (checkSquareDeclined(board, file)) {
                    return MoveType.EN_PASSANT_DECLINED;
                }
            }
        }
        return MoveType.OTHER;
    }
}
