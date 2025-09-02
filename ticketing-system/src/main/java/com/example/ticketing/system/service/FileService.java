package com.example.ticketing.system.service;

import com.example.ticketing.system.entity.Ticket;
import com.example.ticketing.system.entity.TicketAttachment;
import com.example.ticketing.system.entity.User;
import com.example.ticketing.system.repository.TicketAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private TicketAttachmentRepository attachmentRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public TicketAttachment saveFile(MultipartFile file, Ticket ticket, User user) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file to disk
        Path filePath = uploadPath.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Save file info to database
        TicketAttachment attachment = new TicketAttachment(
                fileName,
                file.getContentType(),
                file.getSize(),
                filePath.toString(),
                ticket,
                user
        );

        return attachmentRepository.save(attachment);
    }

    public List<TicketAttachment> getTicketAttachments(Ticket ticket) {
        return attachmentRepository.findByTicket(ticket);
    }

    public byte[] getFileContent(Long attachmentId) throws IOException {
        TicketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        Path filePath = Paths.get(attachment.getFilePath());
        return Files.readAllBytes(filePath);
    }

    public TicketAttachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
    }

    public void deleteAttachment(Long attachmentId) throws IOException {
        TicketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        // Delete file from disk
        Path filePath = Paths.get(attachment.getFilePath());
        Files.deleteIfExists(filePath);

        // Delete record from database
        attachmentRepository.delete(attachment);
    }
}
