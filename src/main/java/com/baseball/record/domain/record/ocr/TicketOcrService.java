package com.baseball.record.domain.record.ocr;

import com.baseball.record.domain.record.dto.OcrRequest;
import com.baseball.record.domain.record.dto.OcrResponse;
import org.springframework.stereotype.Service;

@Service
public class TicketOcrService {

    private final VisionApiClient visionApiClient;
    private final TicketTextParser ticketTextParser;

    public TicketOcrService(VisionApiClient visionApiClient, TicketTextParser ticketTextParser) {
        this.visionApiClient = visionApiClient;
        this.ticketTextParser = ticketTextParser;
    }

    public OcrResponse parseTicket(OcrRequest request) {
        String rawText = visionApiClient.detectText(request.imageUrl());
        return ticketTextParser.parse(rawText);
    }
}
