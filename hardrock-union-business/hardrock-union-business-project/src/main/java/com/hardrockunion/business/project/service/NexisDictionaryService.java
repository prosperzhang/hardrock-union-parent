package com.hardrockunion.business.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hardrockunion.business.project.dto.NexisDictionaryOptionResponse;
import com.hardrockunion.business.project.enums.NexisParticipantCompanyType;
import com.hardrockunion.business.project.enums.NexisParticipantRole;
import com.hardrockunion.business.project.enums.NexisRealNameStatus;
import com.hardrockunion.business.project.enums.NexisRecordStatus;
import com.hardrockunion.business.project.enums.NexisSiteWorkScopeType;
import com.hardrockunion.business.project.enums.NexisWorkerAttendanceStatus;
import com.hardrockunion.business.project.enums.NexisWorkerEntryStatus;

@Service
public class NexisDictionaryService {

    public List<NexisDictionaryOptionResponse> listParticipantCompanyTypes() {
        return List.of(NexisParticipantCompanyType.values()).stream()
            .map(item -> toOption(item.getCode(), item.getLabel(), item.getDescription()))
            .toList();
    }

    public List<NexisDictionaryOptionResponse> listParticipantRoles() {
        return List.of(NexisParticipantRole.values()).stream()
            .map(item -> toOption(item.getCode(), item.getLabel(), item.getDescription()))
            .toList();
    }

    public List<NexisDictionaryOptionResponse> listSiteWorkScopeTypes() {
        return List.of(NexisSiteWorkScopeType.values()).stream()
            .map(item -> toOption(item.getCode(), item.getLabel(), item.getDescription()))
            .toList();
    }

    public List<NexisDictionaryOptionResponse> listRecordStatuses() {
        return List.of(NexisRecordStatus.values()).stream()
            .map(item -> toOption(String.valueOf(item.getCode()), item.getLabel(), item.getDescription()))
            .toList();
    }

    public List<NexisDictionaryOptionResponse> listRealNameStatuses() {
        return List.of(NexisRealNameStatus.values()).stream()
            .map(item -> toOption(item.getCode(), item.getLabel(), item.getDescription()))
            .toList();
    }

    public List<NexisDictionaryOptionResponse> listWorkerEntryStatuses() {
        return List.of(NexisWorkerEntryStatus.values()).stream()
            .map(item -> toOption(item.getCode(), item.getLabel(), item.getDescription()))
            .toList();
    }

    public List<NexisDictionaryOptionResponse> listWorkerAttendanceStatuses() {
        return List.of(NexisWorkerAttendanceStatus.values()).stream()
            .map(item -> toOption(item.getCode(), item.getLabel(), item.getDescription()))
            .toList();
    }

    private NexisDictionaryOptionResponse toOption(String code, String label, String description) {
        NexisDictionaryOptionResponse response = new NexisDictionaryOptionResponse();
        response.setCode(code);
        response.setLabel(label);
        response.setDescription(description);
        return response;
    }
}
