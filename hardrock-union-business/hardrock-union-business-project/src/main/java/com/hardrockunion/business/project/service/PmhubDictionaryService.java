package com.hardrockunion.business.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hardrockunion.business.project.dto.PmhubDictionaryOptionResponse;
import com.hardrockunion.business.project.enums.PmhubParticipantCompanyType;
import com.hardrockunion.business.project.enums.PmhubParticipantRole;
import com.hardrockunion.business.project.enums.PmhubRealNameStatus;
import com.hardrockunion.business.project.enums.PmhubRecordStatus;
import com.hardrockunion.business.project.enums.PmhubSiteWorkScopeType;
import com.hardrockunion.business.project.enums.PmhubWorkerAttendanceStatus;
import com.hardrockunion.business.project.enums.PmhubWorkerEntryStatus;

@Service
public class PmhubDictionaryService {

    public List<PmhubDictionaryOptionResponse> listParticipantCompanyTypes() {
        return List.of(PmhubParticipantCompanyType.values()).stream()
            .map(item -> toOption(item.getCode(), item.getLabel(), item.getDescription()))
            .toList();
    }

    public List<PmhubDictionaryOptionResponse> listParticipantRoles() {
        return List.of(PmhubParticipantRole.values()).stream()
            .map(item -> toOption(item.getCode(), item.getLabel(), item.getDescription()))
            .toList();
    }

    public List<PmhubDictionaryOptionResponse> listSiteWorkScopeTypes() {
        return List.of(PmhubSiteWorkScopeType.values()).stream()
            .map(item -> toOption(item.getCode(), item.getLabel(), item.getDescription()))
            .toList();
    }

    public List<PmhubDictionaryOptionResponse> listRecordStatuses() {
        return List.of(PmhubRecordStatus.values()).stream()
            .map(item -> toOption(String.valueOf(item.getCode()), item.getLabel(), item.getDescription()))
            .toList();
    }

    public List<PmhubDictionaryOptionResponse> listRealNameStatuses() {
        return List.of(PmhubRealNameStatus.values()).stream()
            .map(item -> toOption(item.getCode(), item.getLabel(), item.getDescription()))
            .toList();
    }

    public List<PmhubDictionaryOptionResponse> listWorkerEntryStatuses() {
        return List.of(PmhubWorkerEntryStatus.values()).stream()
            .map(item -> toOption(item.getCode(), item.getLabel(), item.getDescription()))
            .toList();
    }

    public List<PmhubDictionaryOptionResponse> listWorkerAttendanceStatuses() {
        return List.of(PmhubWorkerAttendanceStatus.values()).stream()
            .map(item -> toOption(item.getCode(), item.getLabel(), item.getDescription()))
            .toList();
    }

    private PmhubDictionaryOptionResponse toOption(String code, String label, String description) {
        PmhubDictionaryOptionResponse response = new PmhubDictionaryOptionResponse();
        response.setCode(code);
        response.setLabel(label);
        response.setDescription(description);
        return response;
    }
}
