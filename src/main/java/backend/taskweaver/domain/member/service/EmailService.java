package backend.taskweaver.domain.member.service;

import backend.taskweaver.domain.member.dto.*;
import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.member.entity.MemberRefreshToken;
import backend.taskweaver.domain.member.repository.MemberRefreshTokenRepository;
import backend.taskweaver.domain.member.repository.MemberRepository;
import backend.taskweaver.global.code.ErrorCode;
import backend.taskweaver.global.converter.MemberConverter;
import backend.taskweaver.global.exception.handler.BusinessExceptionHandler;
import backend.taskweaver.global.security.TokenProvider;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Random;

@RequiredArgsConstructor
@Service
public class EmailService {
    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;
    private final MemberRepository memberRepository;

    public EmailResponse sendMail(EmailRequest emailRequest) {
        try {
            if (checkDuplication(emailRequest.email())) {
                MimeMessage mimeMessage = emailSender.createMimeMessage();
                String certificationNum = generateCertificationNum();
                Context context = new Context();
                context.setVariable("certificationNum", certificationNum);
                String message = templateEngine.process("sendEmail", context);

                EmailMessage emailMessage = EmailMessage.builder()
                        .to(emailRequest.email())//보내줘야할 사람
                        //TODO: 여기 제목 채우기
                        .subject("[Task Weaver] 본인인증을 위한 인증번호 발송")
                        .message(message)
                        .build();
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
                mimeMessageHelper.setTo(emailMessage.getTo()); // 메일 수신자
                mimeMessageHelper.setSubject(emailMessage.getSubject()); // 메일 제목
                mimeMessageHelper.setText(emailMessage.getMessage(), true); // 메일 본문 내용, HTML 여부
                emailSender.send(mimeMessage);
                return new EmailResponse(emailRequest.email(), certificationNum);
            }
            else {
                throw new BusinessExceptionHandler(ErrorCode.EMAIL_ERROR);
            }

        } catch (MessagingException e) {
            throw new BusinessExceptionHandler(ErrorCode.EMAIL_ERROR);
        }
    }

    public EmailResponse sendMailForPassword(EmailRequest emailRequest, String tempPassword) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            Context context = new Context();
            context.setVariable("tempPassword", tempPassword);
            String message = templateEngine.process("sendEmailForPassword", context);

            EmailMessage emailMessage = EmailMessage.builder()
                    .to(emailRequest.email())//보내줘야할 사람
                    //TODO: 여기 제목 채우기
                    .subject("[Task Weaver] 임시 비밀번호 발급")
                    .message(message)
                    .build();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailMessage.getTo()); // 메일 수신자
            mimeMessageHelper.setSubject(emailMessage.getSubject()); // 메일 제목
            mimeMessageHelper.setText(emailMessage.getMessage(), true); // 메일 본문 내용, HTML 여부
            emailSender.send(mimeMessage);
            return new EmailResponse(emailRequest.email(), tempPassword);

        } catch (MessagingException e) {
            throw new BusinessExceptionHandler(ErrorCode.EMAIL_ERROR);
        }
    }

    public String generateCertificationNum() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10)); // 0부터 9까지의 숫자를 무작위로 생성
        }

        return sb.toString();
    }

    public boolean checkDuplication(String email) {
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new BusinessExceptionHandler(ErrorCode.DUPLICATED_EMAIL);
        }
        return true;
    }

}