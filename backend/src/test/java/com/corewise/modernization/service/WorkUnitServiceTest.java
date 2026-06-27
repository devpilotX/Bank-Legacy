package com.corewise.modernization.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.corewise.modernization.ai.AiClient;
import com.corewise.modernization.common.BadRequestException;
import com.corewise.modernization.domain.model.WorkUnit;
import com.corewise.modernization.repository.WorkUnitRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class WorkUnitServiceTest {

    private WorkUnitRepository repository;
    private ProjectService projects;
    private AiClient aiClient;
    private WorkUnitService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(WorkUnitRepository.class);
        projects = Mockito.mock(ProjectService.class);
        aiClient = Mockito.mock(AiClient.class);
        service = new WorkUnitService(repository, projects, aiClient);
        when(repository.save(any(WorkUnit.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsAUnitInTodo() {
        WorkUnit unit = service.create(1L, "Move balance calc", "COMPUTE X = A + B.",
            null, 5L, "first slice", 5L);
        assertThat(unit.getStatus()).isEqualTo("todo");
        assertThat(unit.getOriginalCode()).isEqualTo("COMPUTE X = A + B.");
    }

    @Test
    void aiDraftFillsTheDraftAndStartsTheUnit() {
        WorkUnit unit = new WorkUnit(1L, null, "t", "COBOL", "todo", 5L, null, 5L);
        when(repository.findById(9L)).thenReturn(Optional.of(unit));
        when(aiClient.complete(anyString(), eq("COBOL"))).thenReturn("class Generated {}");

        WorkUnit result = service.draftTranslation(9L);

        assertThat(result.getAiDraftJava()).isEqualTo("class Generated {}");
        assertThat(result.getStatus()).isEqualTo("in_progress");
    }

    @Test
    void cannotApproveWithoutHumanJava() {
        WorkUnit unit = new WorkUnit(1L, null, "t", "COBOL", "in_review", 5L, null, 5L);
        when(repository.findById(9L)).thenReturn(Optional.of(unit));

        assertThatThrownBy(() -> service.approve(9L, 7L))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void approveMarksDoneAndRecordsWho() {
        WorkUnit unit = new WorkUnit(1L, null, "t", "COBOL", "in_review", 5L, null, 5L);
        unit.setHumanJava("class Reviewed {}");
        when(repository.findById(9L)).thenReturn(Optional.of(unit));

        WorkUnit result = service.approve(9L, 7L);

        assertThat(result.getStatus()).isEqualTo("done");
        assertThat(result.getApprovedBy()).isEqualTo(7L);
        assertThat(result.getApprovedAt()).isNotNull();
    }

    @Test
    void rejectsAnUnknownStatus() {
        assertThatThrownBy(() -> service.moveStatus(9L, "shipped"))
            .isInstanceOf(BadRequestException.class);
    }
}
