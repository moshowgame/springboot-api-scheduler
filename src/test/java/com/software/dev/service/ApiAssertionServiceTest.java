package com.software.dev.service;

import com.software.dev.entity.ApiAssertion;
import com.software.dev.mapper.ApiAssertionMapper;
import com.software.dev.service.impl.ApiAssertionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiAssertionServiceTest {

    @Mock
    private ApiAssertionMapper apiAssertionMapper;

    @InjectMocks
    private ApiAssertionServiceImpl apiAssertionService;

    @Test
    void testSaveOrUpdate_NewAssertion() {
        // Given
        ApiAssertion assertion = new ApiAssertion();
        assertion.setTaskId("task-123");
        assertion.setAssertionType("HTTP_CODE");
        assertion.setExpectedValue("200");

        when(apiAssertionMapper.findByTaskId("task-123")).thenReturn(Arrays.asList());
        when(apiAssertionMapper.insert(any(ApiAssertion.class))).thenReturn(1);

        // When
        int result = apiAssertionService.saveOrUpdate(assertion);

        // Then
        assertEquals(1, result);
        verify(apiAssertionMapper).insert(any(ApiAssertion.class));
        verify(apiAssertionMapper, never()).update(any(ApiAssertion.class));
    }

    @Test
    void testSaveOrUpdate_ExistingAssertion() {
        // Given
        ApiAssertion existingAssertion = new ApiAssertion();
        existingAssertion.setId("existing-123");
        existingAssertion.setTaskId("task-123");
        
        ApiAssertion newAssertion = new ApiAssertion();
        newAssertion.setTaskId("task-123");
        newAssertion.setAssertionType("HTTP_CODE");
        newAssertion.setExpectedValue("500");

        when(apiAssertionMapper.findByTaskId("task-123")).thenReturn(Arrays.asList(existingAssertion));
        when(apiAssertionMapper.update(any(ApiAssertion.class))).thenReturn(1);

        // When
        int result = apiAssertionService.saveOrUpdate(newAssertion);

        // Then
        assertEquals(1, result);
        verify(apiAssertionMapper).update(any(ApiAssertion.class));
        verify(apiAssertionMapper, never()).insert(any(ApiAssertion.class));
    }
}