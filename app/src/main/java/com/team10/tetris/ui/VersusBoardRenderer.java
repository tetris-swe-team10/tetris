package com.team10.tetris.ui;

import com.team10.tetris.game.*;
import com.team10.tetris.game.versus.AttackLines;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** B의 공용 렌더러가 준비될 때까지 대전 화면에서 주입해 쓰는 구현. */
public final class VersusBoardRenderer implements BoardRenderer {
    private BlockPalette palette = BlockPalette.NORMAL;
    public void setPalette(BlockPalette palette) { this.palette = palette; }
    public void drawBoard(Canvas canvas, GameSnapshot s) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double size = canvas.getWidth() / Board.WIDTH;
        g.setFill(Color.web(s.blackoutActive() ? "#010205" : "#050b14")); g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setStroke(Color.web("#344155"));
        for(int r=0;r<Board.HEIGHT;r++) for(int c=0;c<Board.WIDTH;c++) g.strokeRect(c*size,r*size,size,size);
        PieceSnapshot current = s.currentBlock(), ghost = s.ghostBlock();
        if (ghost != null && ghost.row() > current.row()) {
            int left = Board.WIDTH, right = 0;
            for (int r = 0; r < current.shape().size(); r++)
                for (int c = 0; c < current.shape().get(r).size(); c++)
                    if (current.shape().get(r).get(c) != 0) { left = Math.min(left, current.col() + c); right = Math.max(right, current.col() + c + 1); }
            g.setStroke(Color.web("#64748b"));
            for(int side=0;side<2;side++) {
                int edge=side==0?left:right-1;
                int bottom=0, top=Board.HEIGHT;
                for(int r=0;r<current.shape().size();r++) {
                    int c=edge-current.col();
                    if(current.shape().get(r).get(c)!=0) { bottom=Math.max(bottom,r+1); top=Math.min(top,r); }
                }
                double x=(side==0?left:right)*size;
                if(ghost.row()+top>current.row()+bottom)
                    g.strokeLine(x,(current.row()+bottom)*size,x,(ghost.row()+top)*size);
            }
            piece(g, ghost, size, 0, 0, true);
        }
        double centerX = 0, centerY = 0; int occupied = 0;
        if (current != null) {
            int minR = 99, maxR = -1, minC = 99, maxC = -1;
            for (int r = 0; r < current.shape().size(); r++)
                for (int c = 0; c < current.shape().get(r).size(); c++)
                    if (current.shape().get(r).get(c) != 0) { minR = Math.min(minR, r); maxR = Math.max(maxR, r); minC = Math.min(minC, c); maxC = Math.max(maxC, c); occupied++; }
            centerX = current.col() + (minC + maxC + 1) / 2.0;
            centerY = current.row() + (minR + maxR + 1) / 2.0;
        }
        g.save();
        if (s.blackoutActive() && occupied > 0) {
            g.beginPath(); g.arc(centerX * size, centerY * size, 2 * size, 2 * size, 0, 360); g.closePath(); g.clip();
            g.setFill(Color.web("#101d30")); g.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
        }
        for (int r = 0; r < Board.HEIGHT; r++) for (int c = 0; c < Board.WIDTH; c++) {
            if (s.cells().get(r).get(c) == 0) continue;
            cell(g, c * size, r * size, size, s.isGarbage(r,c) ? BlockStyle.plain(Color.GRAY) : palette.styleOf(s.cellTypes().get(r).get(c)), false);
            if (s.clearingRows().contains(r) || s.effectFlashVisible() && s.effectCells().contains(new CellPosition(r,c))) {
                g.setFill(Color.rgb(255,255,255,.65)); g.fillRect(c * size, r * size, size, size);
            }
        }
        g.restore();
        if (current != null) piece(g, current, size, 0, 0, false);
    }
    public void drawNext(Canvas canvas, GameSnapshot s) {
        GraphicsContext g = canvas.getGraphicsContext2D(); g.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        if (s.nextBlock() != null) piece(g, s.nextBlock(), canvas.getWidth()/5, -s.nextBlock().col(), -s.nextBlock().row(), false);
    }
    private void piece(GraphicsContext g, PieceSnapshot p, double size, double dx, double dy, boolean ghost) {
        for (int r = 0; r < p.shape().size(); r++) for (int c = 0; c < p.shape().get(r).size(); c++)
            if (p.shape().get(r).get(c) != 0) cell(g,(p.col()+c+dx)*size,(p.row()+r+dy)*size,size,palette.styleOf(p.type()),ghost);
        if (!ghost && p.item() != null) {
            var marker = p.item();
            g.setFill(Color.WHITE);
            if (p.isItemPiece()) {
                for(int r=0;r<p.shape().size();r++) for(int c=0;c<p.shape().get(r).size();c++)
                    if(p.shape().get(r).get(c)!=0) g.fillText(marker.symbol(),(p.col()+c+dx+.25)*size,(p.row()+r+dy+.75)*size);
            } else g.fillText(marker.symbol(), (p.col()+marker.col()+dx+.25)*size,(p.row()+marker.row()+dy+.75)*size);
        }
    }
    private void cell(GraphicsContext g, double x, double y, double size, BlockStyle style, boolean ghost) {
        if (ghost) { g.setStroke(style.fill()); g.strokeRect(x+2,y+2,size-4,size-4); return; }
        g.setFill(style.fill()); g.fillRect(x+1,y+1,size-2,size-2);
        g.save(); g.beginPath(); g.rect(x+1,y+1,size-2,size-2); g.clip();
        g.setStroke(style.patternColor()); g.setFill(style.patternColor()); g.setLineWidth(1);
        for(double offset=size/4;offset<size;offset+=size/4) {
            switch(style.pattern()) {
                case HORIZONTAL_LINES -> g.strokeLine(x,y+offset,x+size,y+offset);
                case VERTICAL_LINES -> g.strokeLine(x+offset,y,x+offset,y+size);
                case GRID -> { g.strokeLine(x,y+offset,x+size,y+offset); g.strokeLine(x+offset,y,x+offset,y+size); }
                default -> { }
            }
        }
        for(double offset=0;offset<2*size;offset+=size/3) {
            switch(style.pattern()) {
                case DIAGONAL_RIGHT -> g.strokeLine(x+offset-size,y+size,x+offset,y);
                case DIAGONAL_LEFT -> g.strokeLine(x+offset-size,y,x+offset,y+size);
                case CROSS_DIAGONAL -> {g.strokeLine(x+offset-size,y+size,x+offset,y); g.strokeLine(x+offset-size,y,x+offset,y+size);}
                default -> { }
            }
        }
        if(style.pattern()==BlockPattern.DOTS) for(double r:new double[]{.3,.7}) for(double c:new double[]{.3,.7})
            g.fillOval(x+c*size-size*.08,y+r*size-size*.08,size*.16,size*.16);
        g.restore();
    }
    public void drawPendingAttack(Canvas canvas, AttackLines pending) {
        GraphicsContext g = canvas.getGraphicsContext2D(); g.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        double size = canvas.getWidth()/Board.WIDTH;
        for (int r=0;r<pending.size();r++) for(int c=0;c<Board.WIDTH;c++)
            if(pending.rows().get(r).get(c)) cell(g,c*size,r*size,size,BlockStyle.plain(Color.GRAY),false);
    }
}
