package com.Sergebaranov1986.TelegramJavaBot;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    public UpdateConsumer() {
        this.telegramClient = new OkHttpTelegramClient("8264566602:AAHk3XG6AVrMnkb_PRkVmtlR6pOxidccEic");
    }

    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            if (messageText.equals("/start")) {
                sendMainMenu(chatId);
            } else if (messageText.equals("/keyboard")) {
                sendReplyKeyboard(chatId);
            } else if (messageText.equals("Hello!")) {
                sendMyName(chatId, update.getMessage().getFrom());
            }
            else if (messageText.equals("Picture")) {
                sendImage(chatId);
            }
            else if (messageText.equals("Random")) {
                sendRandom(chatId);
            }
            else {
                sendMessage(chatId, "I dont understand");

            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    @SneakyThrows
    private void sendReplyKeyboard(Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId.toString())
                .text("this is ordinary keyboard sample")
                .build();
        List<KeyboardRow> rows = List.of(new KeyboardRow("Hello!", "Picture"));
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(rows);
        sendMessage.setReplyMarkup(markup);
        telegramClient.execute(sendMessage);
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        var user = callbackQuery.getFrom();

        switch (data) {
            case "my_name" -> sendMyName(chatId, user);
            case "random" -> sendRandom(chatId);
            case "long_process" -> sendImage(chatId);
            default -> sendMessage(chatId, "Unknown command");

        }
    }

    @SneakyThrows
    private void sendMessage(Long chatId, String messageText) {
        SendMessage sendMessage = SendMessage.builder()
                .text(messageText)
                .chatId(chatId)
                .build();
        telegramClient.execute(sendMessage);
    }

    private void sendImage(Long chatId) {
        sendMessage(chatId, "Image uploading started");
        new Thread(() -> {
            var imageUrl = "https://avatars.mds.yandex.net/i?id=9c23048bace71833520a4234dec82dff_l-5482808-images-thumbs&n=13";
            try {
                URL url = new URL(imageUrl);
                var inputStream = url.openStream();
                SendPhoto sendPhoto = SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(inputStream, "random.jpg"))
                        .caption("Your random image :")
                        .build();
                telegramClient.execute(sendPhoto);
            } catch (IOException | TelegramApiException e) {
                throw new RuntimeException(e);
            }

        }).start();

    }

    private void sendRandom(Long chatId) {
        var randomInt = ThreadLocalRandom.current().nextInt();
        sendMessage(chatId, "Your random number is: " + randomInt);

    }

    private void sendMyName(Long chatId, User user) {
        var text = "hello!\n\n Your name is: %s\n your nickname is: %s"
                .formatted(
                        user.getFirstName() + " " + user.getLastName(),
                        user.getUserName()
                );
        sendMessage(chatId, text);

    }

    @SneakyThrows
    private void sendMainMenu(Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .text("Welcome! choose one action from the following list : ")
                .chatId(chatId)
                .build();
        var button1 = InlineKeyboardButton.builder()
                .text("What is my name")
                .callbackData("my_name")
                .build();
        var button2 = InlineKeyboardButton.builder()
                .text("Random number")
                .callbackData("random")
                .build();
        var button3 = InlineKeyboardButton.builder()
                .text("Starting long process")
                .callbackData("long_process")
                .build();
        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
        sendMessage.setReplyMarkup(markup);
        telegramClient.execute(sendMessage);
    }
}
