package com.project.personal_assistant.repo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.project.personal_assistant.model.Expense;

@SpringBootTest
public class ExpenseRepoTest {
    @Autowired
    private ExpenseRepo expenseRepo;

    @Test
    void testFindAllIdByChatId() {
        Expense expense = new Expense();
        expense.setChatId(8110527977L); // tera actual chatId
        expense.setAmount(500.0);
        expense.setCategory("food");
        expense.setDescription("test lunch");
        expenseRepo.save(expense);

        // test
        List<String> ids = expenseRepo.findAllIdByChatId(8110527977L);
        System.out.println("IDs: " + ids);

        assertFalse(ids.isEmpty(), "IDs are empty");
        assertNotNull(ids.get(0));
        expenseRepo.deleteById(ids.get(0));
    }

    @AfterEach
    void cleanUp() {
        expenseRepo.deleteByChatIdAndDescription(8110527977L, "test lunch");
    }

}
