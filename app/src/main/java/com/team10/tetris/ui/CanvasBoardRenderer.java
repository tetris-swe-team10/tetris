package com.team10.tetris.ui;

import com.team10.tetris.game.Board;
import com.team10.tetris.game.GameSnapshot;
import com.team10.tetris.game.PieceSnapshot;
import com.team10.tetris.game.versus.AttackLines;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * {@link BoardRenderer}의 공용 구현. {@link GameView}의 그리기 코드를 그대로 옮겼다.
 *
 * <p>스냅샷만 보고 그리므로 1인 게임, 같은 PC 대전, P2P 상대 보드에서 같이 쓴다.
 * 칸 크기는 캔버스 크기에서 계산한다(보드: 폭 / {@link Board#WIDTH}, NEXT: 짧은 변 / 5).
 * 1인 게임의 280×560 보드와 120×120 NEXT 캔버스에서는 기존과 같은 28px·24px 칸이 된다.
 */
public final class CanvasBoardRenderer implements BoardRenderer {

    // 무늬 간격은 칸 폭의 약 22%, 선 굵기는 1.75px
    private static final double PATTERN_SPACING_RATIO = 0.22;
    private static final double PATTERN_LINE_WIDTH = 1.75;
    private static final double OUTLINE_WIDTH = 1.5;
    private static final int PREVIEW_CELLS = 5;

    static final Color BOARD_BACKGROUND = Color.web("#060B16");
    static final Color BLACKOUT_BACKGROUND = Color.web("#010205");
    static final Color PANEL_BACKGROUND = Color.web("#0B1320");

    /** 상대에게 받은 공격 줄의 회색 칸(V-5). 팔레트와 관계없이 같은 회색이다. */
    static final BlockStyle GARBAGE = BlockStyle.plain(Color.web("#6B7280"));

    private BlockPalette palette = BlockPalette.NORMAL;

    @Override
    public void setPalette(BlockPalette palette) {
        this.palette = palette == null ? BlockPalette.NORMAL : palette;
    }

    @Override
    public void drawBoard(Canvas canvas, GameSnapshot snapshot) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double cell = canvas.getWidth() / Board.WIDTH;

        gc.setFill(snapshot.blackoutActive() ? BLACKOUT_BACKGROUND : BOARD_BACKGROUND);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // 안내선은 고정 블록 뒤에 그려 쌓인 블록을 가리지 않는다.
        PieceSnapshot ghost = snapshot.ghostBlock();
        if (ghost != null) drawDropGuides(gc, snapshot.currentBlock(), ghost, cell);

        // 고정 블록은 조작 블록의 실제 점유 영역 중심에서 지름 4칸만 보인다.
        gc.save();
        if (snapshot.blackoutActive() && snapshot.currentBlock() != null) {
            PieceSnapshot focus = snapshot.currentBlock();
            int minRow = Integer.MAX_VALUE, maxRow = -1, minCol = Integer.MAX_VALUE, maxCol = -1;
            for (int r = 0; r < focus.shape().size(); r++) {
                for (int c = 0; c < focus.shape().get(r).size(); c++) {
                    if (focus.shape().get(r).get(c) != 0) {
                        minRow = Math.min(minRow, r); maxRow = Math.max(maxRow, r);
                        minCol = Math.min(minCol, c); maxCol = Math.max(maxCol, c);
                    }
                }
            }
            double cx = (focus.col() + (minCol + maxCol + 1) / 2.0) * cell;
            double cy = (focus.row() + (minRow + maxRow + 1) / 2.0) * cell;
            gc.beginPath();
            gc.arc(cx, cy, 2 * cell, 2 * cell, 0, 360);
            gc.closePath();
            gc.clip();
            gc.setFill(Color.web("#101D30"));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
        // 고정된 블록 (받은 공격 줄은 회색)
        for (int row = 0; row < Board.HEIGHT; row++) {
            for (int col = 0; col < Board.WIDTH; col++) {
                if (snapshot.cells().get(row).get(col) != 0) {
                    BlockStyle style = snapshot.isGarbage(row, col)
                            ? GARBAGE
                            : palette.styleOf(snapshot.cellTypes().get(row).get(col));
                    drawCell(gc, row, col, cell, style);
                }
            }
        }

        gc.restore();

        if (ghost != null && ghost.row() > snapshot.currentBlock().row()) drawGhost(gc, ghost, cell);

        // 현재 블록
        if (snapshot.currentBlock() != null) {
            PieceSnapshot block = snapshot.currentBlock();
            var shape = block.shape();
            BlockStyle style = palette.styleOf(block.type());

            for (int row = 0; row < shape.size(); row++) {
                for (int col = 0; col < shape.get(row).size(); col++) {
                    if (shape.get(row).get(col) == 1) {
                        drawCell(gc, block.row() + row, block.col() + col, cell, style);
                    }
                }
            }
            drawItemMarkers(gc, block, block.col() * cell, block.row() * cell, cell);
        }

        // 삭제 예정 행은 압축 전 위치에 밝은 색으로 표시한다.
        gc.setFill(Color.rgb(255, 255, 255, 0.75));
        for (int row : snapshot.clearingRows()) {
            gc.fillRect(0, row * cell, canvas.getWidth(), cell);
        }
        // 줄 삭제와 같은 흰색 강조를 실제 폭발로 없어질 칸에만 두 번 깜빡인다.
        if (snapshot.effectFlashVisible()) {
            for (var target : snapshot.effectCells()) {
                gc.fillRect(target.col() * cell, target.row() * cell, cell, cell);
            }
        }

        // 어두운 화면에서도 셀 경계를 구분할 수 있도록 밝기를 확보한다.
        gc.setStroke(Color.web("#344155"));

        for (int row = 0; row < Board.HEIGHT; row++) {
            for (int col = 0; col < Board.WIDTH; col++) {
                gc.strokeRect(col * cell, row * cell, cell, cell);
            }
        }
    }

    @Override
    public void drawNext(Canvas canvas, GameSnapshot snapshot) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(PANEL_BACKGROUND);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        PieceSnapshot nextBlock = snapshot.nextBlock();
        if (nextBlock == null) return;
        var shape = nextBlock.shape();
        BlockStyle style = palette.styleOf(nextBlock.type());

        int previewCellSize = (int) (Math.min(canvas.getWidth(), canvas.getHeight()) / PREVIEW_CELLS);

        int startX = ((int) canvas.getWidth() - shape.get(0).size() * previewCellSize) / 2;
        int startY = ((int) canvas.getHeight() - shape.size() * previewCellSize) / 2;

        for (int row = 0; row < shape.size(); row++) {
            for (int col = 0; col < shape.get(row).size(); col++) {
                if (shape.get(row).get(col) == 1) {
                    drawBlock(gc,
                            startX + col * previewCellSize,
                            startY + row * previewCellSize,
                            previewCellSize - 2,
                            style);
                }
            }
        }
        drawItemMarkers(gc, nextBlock, startX, startY, previewCellSize);
    }

    /** 대기 줄을 캔버스 아래쪽에 붙여 그린다. 마지막 줄(가장 아래에 들어갈 줄)이 맨 아래에 온다. */
    @Override
    public void drawPendingAttack(Canvas canvas, AttackLines pending) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(PANEL_BACKGROUND);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (pending == null || pending.isEmpty()) return;

        double cell = canvas.getWidth() / Board.WIDTH;
        double bottom = canvas.getHeight();
        for (int r = 0; r < pending.size(); r++) {
            double y = bottom - (pending.size() - r) * cell;
            for (int c = 0; c < Board.WIDTH; c++) {
                if (pending.rows().get(r).get(c)) {
                    drawBlock(gc, c * cell + 1, y + 1, cell - 2, GARBAGE);
                }
            }
        }
    }

    /** 좌우 끝 열의 실제 점유 칸을 기준으로 현재 블록 아래와 착지 위치 위를 연결한다. */
    private static void drawDropGuides(GraphicsContext gc, PieceSnapshot current, PieceSnapshot ghost, double cell) {
        if (ghost.row() <= current.row()) return;
        int left = Integer.MAX_VALUE, right = -1;
        for (var row : current.shape()) {
            for (int col = 0; col < row.size(); col++) {
                if (row.get(col) != 0) { left = Math.min(left, col); right = Math.max(right, col); }
            }
        }
        gc.save();
        gc.setStroke(Color.web("#CBD5E1", 0.42));
        gc.setLineWidth(1);
        gc.setLineDashes(5, 5);
        for (int side = 0; side < 2; side++) {
            int col = side == 0 ? left : right;
            int top = -1, bottom = -1;
            for (int row = 0; row < current.shape().size(); row++) {
                if (current.shape().get(row).get(col) != 0) {
                    if (top < 0) top = row;
                    bottom = row;
                }
            }
            double start = (current.row() + bottom + 1) * cell;
            double end = (ghost.row() + top) * cell;
            double x = (current.col() + col + side) * cell + (side == 0 ? 2 : -2);
            if (end > start) gc.strokeLine(x, start, x, end);
        }
        gc.restore();
    }

    private static void drawGhost(GraphicsContext gc, PieceSnapshot ghost, double cell) {
        gc.save();
        gc.setFill(Color.web("#CBD5E1", 0.08));
        gc.setStroke(Color.web("#CBD5E1", 0.8));
        gc.setLineWidth(1.5);
        for (int row = 0; row < ghost.shape().size(); row++) {
            for (int col = 0; col < ghost.shape().get(row).size(); col++) {
                if (ghost.shape().get(row).get(col) == 0) continue;
                double x = (ghost.col() + col) * cell + 3;
                double y = (ghost.row() + row) * cell + 3;
                gc.fillRect(x, y, cell - 6, cell - 6);
                gc.strokeRect(x, y, cell - 6, cell - 6);
            }
        }
        gc.restore();
    }

    /** 부착형은 한 칸, 블록형 아이템(무게추)은 모든 칸에 문자를 그린다. */
    private static void drawItemMarkers(GraphicsContext gc, PieceSnapshot piece, double x, double y, double size) {
        var item = piece.item();
        if (item == null) return;
        for (int r = 0; r < piece.shape().size(); r++) {
            for (int c = 0; c < piece.shape().get(r).size(); c++) {
                boolean marked = piece.isItemPiece()
                        ? piece.shape().get(r).get(c) != 0
                        : r == item.row() && c == item.col();
                if (marked) drawItemMarker(gc, x + c * size, y + r * size, size, item.symbol());
            }
        }
    }

    /** 문자와 명암 테두리로 구분해 일반·색맹 팔레트 모두에서 읽을 수 있게 한다. */
    private static void drawItemMarker(GraphicsContext gc, double x, double y, double size, String symbol) {
        gc.save();
        double inset = 3;
        gc.setFill(Color.web("#111827"));
        gc.fillRoundRect(x + inset, y + inset, size - 2 * inset, size - 2 * inset, 4, 4);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x + inset, y + inset, size - 2 * inset, size - 2 * inset, 4, 4);
        gc.setFill(Color.WHITE);
        gc.setFont(AppFonts.bold(size * 0.64));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(symbol, x + size / 2, y + size / 2, size - 6);
        gc.restore();
    }

    private static void drawCell(GraphicsContext gc, int row, int col, double cell, BlockStyle style) {
        drawBlock(gc, col * cell + 1, row * cell + 1, cell - 2, style);
    }

    // 블록 한 칸: 채우기 → 무늬 → 외곽선
    private static void drawBlock(GraphicsContext gc, double x, double y, double size, BlockStyle style) {
        gc.setFill(style.fill());
        gc.fillRect(x, y, size, size);

        if (style.pattern() != BlockPattern.NONE) {
            // 무늬가 칸 밖으로 나가지 않도록 칸 영역으로 자름
            gc.save();
            gc.beginPath();
            gc.rect(x, y, size, size);
            gc.closePath();
            gc.clip();

            drawPattern(gc, x, y, size, style);

            gc.restore();
        }

        gc.setStroke(BlockPalette.OUTLINE);
        gc.setLineWidth(OUTLINE_WIDTH);
        gc.strokeRect(x + OUTLINE_WIDTH / 2, y + OUTLINE_WIDTH / 2, size - OUTLINE_WIDTH, size - OUTLINE_WIDTH);
        gc.setLineWidth(1);
    }

    // 무늬는 화면 기준으로 그려서 블록이 회전해도 방향이 바뀌지 않음
    private static void drawPattern(GraphicsContext gc, double x, double y, double size, BlockStyle style) {
        double spacing = size * PATTERN_SPACING_RATIO;

        gc.setStroke(style.patternColor());
        gc.setFill(style.patternColor());
        gc.setLineWidth(PATTERN_LINE_WIDTH);

        switch (style.pattern()) {
            case HORIZONTAL_LINES -> drawHorizontalLines(gc, x, y, size, spacing);
            case VERTICAL_LINES -> drawVerticalLines(gc, x, y, size, spacing);
            case GRID -> {
                drawHorizontalLines(gc, x, y, size, spacing);
                drawVerticalLines(gc, x, y, size, spacing);
            }
            case DIAGONAL_RIGHT -> drawDiagonalLines(gc, x, y, size, spacing, true);
            case DIAGONAL_LEFT -> drawDiagonalLines(gc, x, y, size, spacing, false);
            case CROSS_DIAGONAL -> {
                drawDiagonalLines(gc, x, y, size, spacing, true);
                drawDiagonalLines(gc, x, y, size, spacing, false);
            }
            case DOTS -> drawDots(gc, x, y, size);
            case NONE -> {
            }
        }
    }

    private static void drawHorizontalLines(GraphicsContext gc, double x, double y, double size, double spacing) {
        for (double offset = spacing; offset < size; offset += spacing) {
            gc.strokeLine(x, y + offset, x + size, y + offset);
        }
    }

    private static void drawVerticalLines(GraphicsContext gc, double x, double y, double size, double spacing) {
        for (double offset = spacing; offset < size; offset += spacing) {
            gc.strokeLine(x + offset, y, x + offset, y + size);
        }
    }

    // rising == true 이면 '/', false 이면 '\'
    private static void drawDiagonalLines(
            GraphicsContext gc, double x, double y, double size, double spacing, boolean rising) {
        // 선 사이의 수직 거리가 spacing이 되도록 가로 방향 간격을 넓힘
        double step = spacing * Math.sqrt(2);

        for (double offset = step; offset < size * 2; offset += step) {
            if (rising) {
                gc.strokeLine(x + offset - size, y + size, x + offset, y);
            } else {
                gc.strokeLine(x + offset - size, y, x + offset, y + size);
            }
        }
    }

    private static void drawDots(GraphicsContext gc, double x, double y, double size) {
        double radius = size * 0.11;

        for (double cy : new double[] {0.3, 0.7}) {
            for (double cx : new double[] {0.3, 0.7}) {
                gc.fillOval(x + size * cx - radius, y + size * cy - radius, radius * 2, radius * 2);
            }
        }
    }
}
