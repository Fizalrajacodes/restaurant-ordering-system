package com.restaurant.controller.customer;

import com.restaurant.dtos.customer.TableDTO;
import com.restaurant.service.QRCodeService;
import com.restaurant.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CustomerMenuController {

    private final QRCodeService qrCodeService;
    private final TableService tableService;

    @GetMapping("/menu")
    public String showMenu(@RequestParam("table") String token, Model model) {
        qrCodeService.recordScan(token);

        TableDTO table = tableService.getTableByToken(token);

        model.addAttribute("tableToken", token);
        model.addAttribute("tableNumber", table.getTableNumber());
        model.addAttribute("tableName", table.getTableName());
        model.addAttribute("restaurantName", "Smart Restaurant");
        model.addAttribute("restaurantLogo", "/images/logo.png");
        model.addAttribute("restaurantAddress", "123 Restaurant Street, City");
        model.addAttribute("restaurantPhone", "+91-9876543210");

        return "customer/menu";
    }
}