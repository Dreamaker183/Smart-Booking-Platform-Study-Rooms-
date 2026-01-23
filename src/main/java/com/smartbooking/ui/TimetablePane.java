package com.smartbooking.ui;

import com.smartbooking.domain.Booking;
import com.smartbooking.service.AppServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TimetablePane extends VBox {
    public interface SelectionListener {
        void onRangeSelected(LocalDate date, LocalTime start, LocalTime end);
    }

    private static final int START_HOUR = 8;
    private static final int END_HOUR = 22;
    private static final int SLOT_HEIGHT = 60;
    private static final int DAY_WIDTH = 120;

    private final AppServices services;
    private final long currentUserId;
    private long selectedResourceId = -1;
    private LocalDate viewDate;
    private SelectionListener selectionListener;

    private final GridPane grid = new GridPane();
    private final Label dateRangeLabel = new Label();

    // Selection tracking
    private int dragStartRow = -1;
    private int dragStartCol = -1;
    private int dragCurrentRow = -1;

    public TimetablePane(AppServices services, long currentUserId) {
        this.services = services;
        this.currentUserId = currentUserId;
        this.viewDate = LocalDate.now();
        this.setSpacing(15);
        this.setPadding(new Insets(10));

        setupHeader();
        setupGrid();

        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setPrefHeight(600);
        sp.getStyleClass().add("timetable-scroll");

        this.getChildren().add(sp);
    }

    public void setSelectionListener(SelectionListener listener) {
        this.selectionListener = listener;
    }

    private void setupHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.control.Button prev = new javafx.scene.control.Button("←");
        javafx.scene.control.Button next = new javafx.scene.control.Button("→");
        prev.getStyleClass().add("secondary");
        next.getStyleClass().add("secondary");

        prev.setOnAction(e -> {
            viewDate = viewDate.minusWeeks(1);
            refresh();
        });
        next.setOnAction(e -> {
            viewDate = viewDate.plusWeeks(1);
            refresh();
        });

        dateRangeLabel.getStyleClass().add("subtitle");
        header.getChildren().addAll(prev, next, dateRangeLabel);
        this.getChildren().add(header);
    }

    private void setupGrid() {
        grid.getStyleClass().add("timetable-grid");
        // Time labels column
        for (int h = START_HOUR; h < END_HOUR; h++) {
            Label hourLabel = new Label(String.format("%02d:00", h));
            hourLabel.setPrefHeight(SLOT_HEIGHT);
            hourLabel.getStyleClass().add("hour-label");
            grid.add(hourLabel, 0, h - START_HOUR + 1);
        }

        // Days columns
        refresh();
    }

    public void setResource(long resourceId) {
        this.selectedResourceId = resourceId;
        refresh();
    }

    public void refresh() {
        grid.getChildren().removeIf(node -> GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) > 0);

        LocalDate startOfWeek = viewDate.minusDays(viewDate.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        dateRangeLabel.setText(startOfWeek.format(DateTimeFormatter.ofPattern("MMM dd")) + " - " +
                endOfWeek.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            Label dayLabel = new Label(day.format(DateTimeFormatter.ofPattern("EEE dd")));
            dayLabel.getStyleClass().add("day-header");
            dayLabel.setPrefWidth(DAY_WIDTH);
            dayLabel.setAlignment(Pos.CENTER);
            grid.add(dayLabel, i + 1, 0);

            // Add slots
            for (int h = START_HOUR; h < END_HOUR; h++) {
                final int row = h - START_HOUR + 1;
                final int col = i + 1;
                final LocalDate currentDay = day;

                Pane slot = new Pane();
                slot.setPrefHeight(SLOT_HEIGHT);
                slot.getStyleClass().add("timetable-slot");

                slot.setOnMousePressed(e -> {
                    clearSelection();
                    dragStartRow = row;
                    dragStartCol = col;
                    dragCurrentRow = row;
                    updateSelectionVisuals();
                });

                slot.setOnMouseDragged(e -> {
                    // Find which row the mouse is currently over
                    int currentRow = (int) (e.getSceneY() - slot.localToScene(0, 0).getY() + (row - 1) * SLOT_HEIGHT)
                            / SLOT_HEIGHT + 1;
                    if (currentRow >= 1 && currentRow <= (END_HOUR - START_HOUR)) {
                        dragCurrentRow = currentRow;
                        updateSelectionVisuals();
                    }
                });

                slot.setOnMouseReleased(e -> {
                    if (selectionListener != null && dragStartRow != -1) {
                        int r1 = Math.min(dragStartRow, dragCurrentRow);
                        int r2 = Math.max(dragStartRow, dragCurrentRow);
                        LocalTime s = LocalTime.of(START_HOUR + r1 - 1, 0);
                        LocalTime end = LocalTime.of(START_HOUR + r2, 0);
                        selectionListener.onRangeSelected(currentDay, s, end);
                    }
                    // Keep visuals until next click
                });

                grid.add(slot, col, row);
            }
        }

        if (selectedResourceId != -1) {
            loadBookings();
        }
    }

    private void clearSelection() {
        grid.getChildren().forEach(n -> n.getStyleClass().remove("slot-selecting"));
        dragStartRow = -1;
        dragStartCol = -1;
    }

    private void updateSelectionVisuals() {
        int r1 = Math.min(dragStartRow, dragCurrentRow);
        int r2 = Math.max(dragStartRow, dragCurrentRow);

        grid.getChildren().forEach(node -> {
            Integer col = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);
            if (col != null && row != null && col == dragStartCol && row >= r1 && row <= r2) {
                if (!node.getStyleClass().contains("slot-selecting")) {
                    node.getStyleClass().add("slot-selecting");
                }
            } else {
                node.getStyleClass().remove("slot-selecting");
            }
        });
    }

    private void loadBookings() {
        LocalDate startOfWeek = viewDate.minusDays(viewDate.getDayOfWeek().getValue() - 1);
        LocalDateTime start = startOfWeek.atTime(START_HOUR, 0);
        LocalDateTime end = startOfWeek.plusDays(7).atTime(END_HOUR, 0);

        List<Booking> bookings = services.getBookingService().listBookingsForResource(selectedResourceId, start,
                end);

        for (Booking b : bookings) {
            renderBooking(b, startOfWeek);
        }
    }

    private void renderBooking(Booking b, LocalDate startOfWeek) {
        LocalDate bDate = b.getStartTime().toLocalDate();
        long dayIndex = ChronoUnit.DAYS.between(startOfWeek, bDate);
        if (dayIndex < 0 || dayIndex > 6)
            return;

        double startMin = (b.getStartTime().getHour() - START_HOUR) * 60 + b.getStartTime().getMinute();
        double duration = ChronoUnit.MINUTES.between(b.getStartTime(), b.getEndTime());

        VBox block = new VBox(2);
        block.setPadding(new Insets(4));
        block.getStyleClass().add("booking-block");
        block.setMouseTransparent(true); // Allow clicking through to slots

        boolean isMine = b.getUserId() == currentUserId;
        if (isMine) {
            block.getStyleClass().add("booking-mine");
        } else {
            block.getStyleClass().add("booking-other");
        }

        Label nameLabel = new Label(
                isMine ? b.getStatus().toString() : (b.getUsername() != null ? b.getUsername() : "Occupied"));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
        Label timeLabel = new Label(b.getStartTime().toLocalTime() + "-" + b.getEndTime().toLocalTime());
        timeLabel.setStyle("-fx-font-size: 9px;");

        block.getChildren().addAll(nameLabel, timeLabel);

        block.setTranslateY((startMin / 60.0) * SLOT_HEIGHT);
        block.setPrefHeight((duration / 60.0) * SLOT_HEIGHT);
        block.setPrefWidth(DAY_WIDTH - 4);
        block.setMaxHeight((duration / 60.0) * SLOT_HEIGHT);

        grid.add(block, (int) dayIndex + 1, 1);
        GridPane.setRowSpan(block, END_HOUR - START_HOUR); // Span across all rows to use translateY for precision
    }
}
