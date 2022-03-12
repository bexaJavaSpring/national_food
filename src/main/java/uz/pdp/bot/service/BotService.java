package uz.pdp.bot.service;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.bot.enums.BotState;
import uz.pdp.bot.util.BotConstants;
import uz.pdp.bot.util.BotMenu;
import uz.pdp.model.*;
import uz.pdp.model.User;
import uz.pdp.repository.Database;
import uz.pdp.service.ProductService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BotService {
    public static SendMessage start(Update update) {

        registerUser(update);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setText(BotConstants.MENU_HEADER);
        sendMessage.setReplyMarkup(getMenuKeyboard());

        return sendMessage;
    }

    public static SendMessage menu(Long chatId) {
        for (User user : Database.users) {
            if (user.getChatId().equals(chatId)) {
                user.setBotState(BotState.SHOW_MENU);
                Database.writeDataToJsonFile("users");
                break;
            }
        }

        SendMessage sendMessage = new SendMessage();

        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(BotConstants.MENU_HEADER);
        sendMessage.setReplyMarkup(getInlineKeyboardMarkupFromList(Database.categories,
                BotConstants.CATEGORY_PEREFIX, false));

        return sendMessage;
    }

    public static SendMessage settings(Long chatId) {
        for (User user : Database.users) {
            if (user.getChatId().equals(chatId)) {
                user.setBotState(BotState.CHANGE_SETTINGS);
                Database.writeDataToJsonFile("users");
                break;
            }
        }

        SendMessage sendMessage = new SendMessage();

        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Bu yerda tilni tanlash bo'lishi kerak edi.");

        return sendMessage;
    }

    public static EditMessageText showProducts(Message message, Integer categoryId) {
        for (User user : Database.users) {
            if (user.getChatId().equals(message.getChatId())) {
                user.setBotState(BotState.SHOW_PRODUCTS);
                Database.writeDataToJsonFile("users");
                break;
            }
        }

        List<Product> productsByCategory = ProductService.getProductsByCategory(categoryId);

        EditMessageText editMessageText = new EditMessageText();

        editMessageText.setChatId(String.valueOf(message.getChatId()));
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setParseMode(ParseMode.MARKDOWN);
        editMessageText.setText(BotConstants.MENU_HEADER);

        InlineKeyboardMarkup inlineKeyboardMarkup =
                getInlineKeyboardMarkupFromList(productsByCategory, BotConstants.PRODUCT_PEREFIX
                        , true);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);

        return editMessageText;
    }


    public static SendPhoto showProductDetail(Long chatId, Integer productId) {

        for (User user : Database.users) {
            if (user.getChatId().equals(chatId)) {
                user.setBotState(BotState.SELECT_PRODUCT);
                Database.writeDataToJsonFile("users");
                break;
            }
        }


        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));

        for (Product product : Database.products) {
            if (product.getId().equals(productId)) {
//                sendPhoto.setPhoto(new InputFile(new File(product.getImageUrl())));
                sendPhoto.setPhoto(new InputFile(product.getImageUrl()));
                sendPhoto.setCaption(product.getPrice() + " so'm \n\n" +
                        product.getName() + " miqdorini tanlang: ");
                break;
            }
        }

        // product id + amount
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();


        for (int i = 1, count = 1; i <= 3; i++) {
            List<InlineKeyboardButton> buttonList = new ArrayList<>();
            for (int j = 1; j <= 3; j++) {
                InlineKeyboardButton button = new InlineKeyboardButton(count + " ta");
                button.setCallbackData(BotConstants.PRODUCT_AMOUNT_PEREFIX + BotConstants.SEPARATOR + productId
                        + BotConstants.SEPARATOR + count);

                buttonList.add(button);

                count++;
            }
            inlineKeyboard.add(buttonList);
        }

        List<InlineKeyboardButton> buttonList = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton("Ortga qaytish");
        button.setCallbackData(BotConstants.PRODUCT_AMOUNT_PEREFIX + BotConstants.SEPARATOR + productId
                + BotConstants.SEPARATOR + "0");
        buttonList.add(button);
        inlineKeyboard.add(buttonList);

        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);

        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);

        return sendPhoto;
    }


    //=======================================

    private static void registerUser(Update update) {

        org.telegram.telegrambots.meta.api.objects.User from = update.getMessage().getFrom();
        boolean hasUser = false;

        for (User user : Database.users) {
            if (user.getChatId() != null && user.getChatId().equals(update.getMessage().getChatId())) {
                hasUser = true;
                user.setId(from.getId());
                user.setChatId(update.getMessage().getChatId());
                user.setUsername(from.getUserName() != null ? from.getUserName() : "");
                user.setFirstName(from.getFirstName() != null ? from.getFirstName() : "");
                user.setLastName(from.getLastName() != null ? from.getLastName() : "");
                user.setPhoneNumber(update.getMessage().getContact() != null ?
                        update.getMessage().getContact().getPhoneNumber() : "");
                break;
            }
        }

        if (!hasUser) {
            User newUser = new User(from.getId(), update.getMessage().getChatId());
            newUser.setUsername(from.getUserName() != null ? from.getUserName() : "");
            newUser.setFirstName(from.getFirstName() != null ? from.getFirstName() : "");
            newUser.setLastName(from.getLastName() != null ? from.getLastName() : "");
            newUser.setPhoneNumber(update.getMessage().getContact() != null ?
                    update.getMessage().getContact().getPhoneNumber() : "");

            Database.users.add(newUser);

            // create cart
            Cart newCart = new Cart(newUser.getId());
            Database.carts.add(newCart);
        }

        Database.writeDataToJsonFile("users");
        Database.writeDataToJsonFile("carts");
    }

    private static ReplyKeyboardMarkup getMenuKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardRow keyboardRow1 = new KeyboardRow();
        keyboardRow1.add(new KeyboardButton(BotMenu.MENU));
        keyboardRowList.add(keyboardRow1);

        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow2.add(new KeyboardButton(BotMenu.CART));
        keyboardRow2.add(new KeyboardButton(BotMenu.SETTINGS));
        keyboardRowList.add(keyboardRow2);

        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        return replyKeyboardMarkup;
    }

    private static InlineKeyboardMarkup getInlineKeyboardMarkupFromList(List list, String perefix, boolean hasBack) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();

        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();

            List<InlineKeyboardButton> buttonList = new ArrayList<>();
            InlineKeyboardButton button = null;
            if (next instanceof Category) {
                button = new InlineKeyboardButton(((Category) next).getPerefix() + ((Category) next).getName());
                button.setCallbackData(perefix + BotConstants.SEPARATOR + ((Category) next).getId());
            } else if (next instanceof Product) {
                button = new InlineKeyboardButton(((Product) next).getName());
                button.setCallbackData(perefix + BotConstants.SEPARATOR + ((Product) next).getId());
            }

            buttonList.add(button);

            if (iterator.hasNext()) {
                next = iterator.next();
                InlineKeyboardButton button1 = null;
                if (next instanceof Category) {
                    button1 =
                            new InlineKeyboardButton(((Category) next).getPerefix() + ((Category) next).getName());
                    button1.setCallbackData(perefix + BotConstants.SEPARATOR + ((Category) next).getId());
                } else if (next instanceof Product) {
                    button1 = new InlineKeyboardButton(((Product) next).getName());
                    button1.setCallbackData(perefix + BotConstants.SEPARATOR + ((Product) next).getId());
                }

                buttonList.add(button1);
            }

            inlineKeyboard.add(buttonList);
        }

        if (hasBack) {
            List<InlineKeyboardButton> buttonList = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton("Ortga qaytish");
            button.setCallbackData(BotConstants.PRODUCT_PEREFIX + BotConstants.SEPARATOR + "0");
            buttonList.add(button);
            inlineKeyboard.add(buttonList);
        }

        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);

        return inlineKeyboardMarkup;
    }


    public static EditMessageText backToMenu(Message message) {
        for (User user : Database.users) {
            if (user.getChatId().equals(message.getChatId())) {
                user.setBotState(BotState.SHOW_MENU);
                Database.writeDataToJsonFile("users");
                break;
            }
        }

        EditMessageText editMessageText = new EditMessageText();

        editMessageText.setParseMode(ParseMode.MARKDOWN);
        editMessageText.setChatId(String.valueOf(message.getChatId()));
        editMessageText.setMessageId(message.getMessageId());
        editMessageText.setText(BotConstants.MENU_HEADER);
        editMessageText.setReplyMarkup(getInlineKeyboardMarkupFromList(Database.categories,
                BotConstants.CATEGORY_PEREFIX, false));

        return editMessageText;
    }

    public static SendMessage addProductToCart(Long chatId, Integer productId, Integer amount) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setText("Savatchaga product qo'shildi.");

        User currentUser = null;
        for (User user : Database.users) {
            if (user.getChatId().equals(chatId)) {
                currentUser = user;
                user.setBotState(BotState.SELECT_COUNT_PRODUCT);
                Database.writeDataToJsonFile("users");
                break;
            }
        }

        if (currentUser == null) {
            sendMessage.setText("User topilmadi.");
            return sendMessage;
        }

        Cart userCart = null;

        for (Cart cart : Database.carts) {
            if (cart.getUserId().equals(currentUser.getId())) {
                userCart = cart;
            }
        }

        if (userCart == null) {
            sendMessage.setText("Userning savatchasi topilmadi.");
            return sendMessage;
        }

        // add to database

        CartProduct cartProduct = null;

        for (CartProduct cartProduct1 : Database.cartProducts) {
            if (cartProduct1.getCartId().equals(userCart.getUserId()) && cartProduct1.getProductId().equals(productId)) {
                cartProduct = cartProduct1;
                cartProduct.setAmount(cartProduct.getAmount() + amount);
                break;
            }
        }

        if (cartProduct == null) {
            cartProduct = new CartProduct(userCart.getUserId(), productId, amount);
            Database.cartProducts.add(cartProduct);
        }

        Database.writeDataToJsonFile("cartproducts");

        return sendMessage;
    }

    public static SendMessage showCart(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        User currentUser = null;
        for (User user : Database.users) {
            if (user.getChatId().equals(chatId)) {
                currentUser = user;
                user.setBotState(BotState.SHOW_CART);
                Database.writeDataToJsonFile("users");
                break;
            }
        }

        if(currentUser == null){
            sendMessage.setText("User topilmadi");
            return sendMessage;
        }

        Cart userCart = null;

        for (Cart cart : Database.carts) {
            if (cart.getUserId().equals(currentUser.getId())) {
                userCart = cart;
            }
        }

        if (userCart == null) {
            sendMessage.setText("Userning savatchasi topilmadi.");
            return sendMessage;
        }

        List<CartProduct> cartProductList = new ArrayList<>();

        for (CartProduct cartProduct : Database.cartProducts) {
            if(cartProduct.getCartId().equals(userCart.getUserId())){
                cartProductList.add(cartProduct);
            }
        }

        if(cartProductList.isEmpty()){
            sendMessage.setText("Savat bo'sh.");
            return sendMessage;
        }

        String text = "*Savatchada:* \n\n";
        Double total = 0d;

        for (int i = 0; i < cartProductList.size(); i++) {
            CartProduct cartProduct = cartProductList.get(i);
            Product product = ProductService.getProductById(cartProduct.getProductId());
            if(product != null){
                text += (i+1)+". *"+product.getName()+ "* x "+cartProduct.getAmount()+ " = "+
                        (product.getPrice()*cartProduct.getAmount())+"\n";
                total += product.getPrice()*cartProduct.getAmount();
            }
        }
        text += "\n*Jami: \t\t"+total+" so'm *";
        sendMessage.setText(text);

        sendMessage.setReplyMarkup(getInlineKeyboardForCart(cartProductList));

        return sendMessage;
    }

    private static InlineKeyboardMarkup getInlineKeyboardForCart(List<CartProduct> cartProductList){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();

        Iterator<CartProduct> iterator = cartProductList.iterator();
        while (iterator.hasNext()) {
            CartProduct cartProduct = iterator.next();
            Product product = ProductService.getProductById(cartProduct.getProductId());

            List<InlineKeyboardButton> buttonList = new ArrayList<>();

            if(product != null){
                InlineKeyboardButton button = new InlineKeyboardButton("❌ "+product.getName());
                button.setCallbackData(BotConstants.CART_PRODUCT_DELETE_PEREFIX+BotConstants.SEPARATOR+
                        cartProduct.getId());
                buttonList.add(button);
            }

            if (iterator.hasNext()){
                cartProduct = iterator.next();
                product = ProductService.getProductById(cartProduct.getProductId());
                if(product != null){
                    InlineKeyboardButton button = new InlineKeyboardButton("❌ "+product.getName());
                    button.setCallbackData(BotConstants.CART_PRODUCT_DELETE_PEREFIX+BotConstants.SEPARATOR+
                            cartProduct.getId());
                    buttonList.add(button);
                }
            }

            inlineKeyboard.add(buttonList);
        }


        // back to menu
        List<InlineKeyboardButton> buttonList = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton("Menuga qaytish");
        button.setCallbackData(BotConstants.TO_MENU);
        buttonList.add(button);
        inlineKeyboard.add(buttonList);

        List<InlineKeyboardButton> buttonList1 = new ArrayList<>();
        // order commit
        InlineKeyboardButton commitButton = new InlineKeyboardButton(BotConstants.ORDER_COMMIT);
        commitButton.setCallbackData(BotConstants.ORDER_COMMIT);
        buttonList1.add(commitButton);
        // order cancel
        InlineKeyboardButton cancelButton = new InlineKeyboardButton(BotConstants.ORDER_CANCEL);
        cancelButton.setCallbackData(BotConstants.ORDER_CANCEL);
        buttonList1.add(cancelButton);

        inlineKeyboard.add(buttonList1);

        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);
        return inlineKeyboardMarkup;
    }

    public static SendMessage orderCancel(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        Database.cartProducts.removeIf(cartProduct -> cartProduct.getCartId().equals(chatId));
        Database.writeDataToJsonFile("cartproducts");

        sendMessage.setText("Savatcha bo'shatildi.");

        return sendMessage;

    }

    public static SendMessage orderCommit(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        Database.cartProducts.removeIf(cartProduct -> cartProduct.getCartId().equals(chatId));
        Database.writeDataToJsonFile("cartproducts");

        sendMessage.setText(" kiriting:");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton shareContactButton = new KeyboardButton("Dastavka berish ");
        shareContactButton.setRequestContact(true);

        keyboardRow.add(shareContactButton);
        keyboardRowList.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    public static SendMessage gettedContactAskLocation(Long chatId, Contact contact) {

        for (User user : Database.users) {
            if(user.getChatId().equals(chatId)){
                user.setFirstName(contact.getFirstName());
                user.setLastName(contact.getLastName());
                user.setPhoneNumber(contact.getPhoneNumber());
                break;
            }
        }

        Database.writeDataToJsonFile("users");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);


        sendMessage.setText("Xohlasangiz Locationni jo'nating:");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton shareContactButton = new KeyboardButton("Locationni jo'natish");
        shareContactButton.setRequestLocation(true);

        keyboardRow.add(shareContactButton);
        keyboardRowList.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    public static SendMessage gettedLocationAskPhotoDocument(Long chatId, Location location) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        String text = "*Sizning joylashuvingiz: *\n\n";
        text += "*Lat: *"+location.getLatitude()+" \n";
        text += "*Lng: *"+location.getLongitude()+" \n\n";

        text += "Biror fayl yuboring.";

        sendMessage.setText(text);

        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));

        return sendMessage;
    }

}



