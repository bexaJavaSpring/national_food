package uz.pdp.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.bot.enums.BotState;
import uz.pdp.bot.service.BotService;
import uz.pdp.bot.util.BotConstants;
import uz.pdp.bot.util.BotMenu;
import uz.pdp.bot.util.BotSettings;
import uz.pdp.service.CartProductService;

import static uz.pdp.bot.enums.BotState.START;

public class NationalFoodBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()){
            Message message = update.getMessage();
            SendMessage sendMessage = null;

            if(message.hasText()){

                String text = message.getText();

                switch (text){
                    case BotMenu.START:{
                        sendMessage = BotService.start(update);
                    }break;
                    case BotMenu.MENU:{
                        sendMessage = BotService.menu(message.getChatId());
                    }break;
                    case BotMenu.CART:{
                        sendMessage = BotService.showCart(message.getChatId());
                    }break;
                    case BotMenu.SETTINGS:{
                        sendMessage = BotService.settings(message.getChatId());
                    }break;
                }

            }
            else if(message.hasContact()){
                Contact contact = message.getContact();
                sendMessage = BotService.gettedContactAskLocation(message.getChatId(), contact);
            }
            else if(message.hasLocation()){
                Location location = message.getLocation();
                sendMessage = BotService.gettedLocationAskPhotoDocument(message.getChatId(),
                        location);

            }
            else if(message.hasDocument()){
                Document document = message.getDocument();

                GetFile getFile = new GetFile(document.getFileId());
                try {
                    File executeFile = execute(getFile);
                    downloadFile(executeFile,
                            new java.io.File("src/main/resources/documents/"+document.getFileName()));

                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                sendMessage = BotService.start(update);
            }

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        else if(update.hasCallbackQuery()){

            String data = update.getCallbackQuery().getData();
            Message message = update.getCallbackQuery().getMessage();

            if(data.startsWith(BotConstants.CATEGORY_PEREFIX)){
                Integer categoryId = Integer.parseInt(data.split(BotConstants.SEPARATOR)[1]);

                EditMessageText editMessage = BotService.showProducts(message, categoryId);

                try {
                    execute(editMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if(data.startsWith(BotConstants.PRODUCT_PEREFIX)){

                Long chatId = message.getChatId();
                Integer productId = Integer.parseInt(data.split(BotConstants.SEPARATOR)[1]);

                if(productId.equals(0)){
                    EditMessageText editMessageText = BotService.backToMenu(message);
                    try {
                        execute(editMessageText);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }else{
                    SendPhoto sendPhoto = BotService.showProductDetail(chatId, productId);

                    try {
                        execute(sendPhoto);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                    // delete old message
                    DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(),
                            message.getMessageId());
                    try {
                        execute(deleteMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if(data.startsWith(BotConstants.PRODUCT_AMOUNT_PEREFIX)){
                // amount / productId / amount (0=back)
                String[] strings = data.split(BotConstants.SEPARATOR);

                Long chatId = message.getChatId();
                Integer productId = Integer.parseInt(strings[1]);
                Integer amount = Integer.parseInt(strings[2]);

                SendMessage sendMessage = null;
                if(amount.equals(0)){
                    sendMessage = BotService.menu(chatId);
                }else {
                    sendMessage = BotService.addProductToCart(chatId, productId,
                            amount);
                }
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                // delete old message
                DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(),
                        message.getMessageId());
                try {
                    execute(deleteMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if(data.startsWith(BotConstants.CART_PRODUCT_DELETE_PEREFIX)){
                Long chatId = message.getChatId();

                // delete old message
                DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(),
                        message.getMessageId());
                try {
                    execute(deleteMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                // delete / 10 (=cartproduct.getid)

                Long cartProductId = Long.parseLong(data.split(BotConstants.SEPARATOR)[1]);
                CartProductService.deleteCartProductById(cartProductId);

                SendMessage sendMessage = BotService.showCart(chatId);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }
            else if(data.equals(BotConstants.TO_MENU)){
                Long chatId = message.getChatId();

                // delete old message
                DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(),
                        message.getMessageId());
                try {
                    execute(deleteMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                SendMessage sendMessage = BotService.menu(chatId);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }
            else if(data.equals(BotConstants.ORDER_CANCEL)){
                Long chatId = message.getChatId();

                // delete old message
                DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(),
                        message.getMessageId());
                try {
                    execute(deleteMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                SendMessage sendMessage = BotService.orderCancel(chatId);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if(data.equals(BotConstants.ORDER_COMMIT)){
                Long chatId = message.getChatId();

                // delete old message
                DeleteMessage deleteMessage = new DeleteMessage(message.getChatId().toString(),
                        message.getMessageId());
                try {
                    execute(deleteMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                SendMessage sendMessage = BotService.orderCommit(chatId);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }
        }
    }
        @Override
    public String getBotUsername() {
        return BotSettings.bot_username;
    }

    @Override
    public String getBotToken() {
        return BotSettings.bot_token;
    }
}

