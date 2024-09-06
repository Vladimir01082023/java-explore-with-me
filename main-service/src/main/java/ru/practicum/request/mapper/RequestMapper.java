package ru.practicum.request.mapper;


import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

public class RequestMapper {

    public static ParticipationRequestDto toParticipationRequestDto(Request request) {
        ParticipationRequestDto participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(request.getId());
        participationRequestDto.setCreated(request.getCreated());
        participationRequestDto.setRequester(request.getRequester().getId());
        participationRequestDto.setEvent(request.getEvent().getId());
        participationRequestDto.setStatus(request.getStatus());
        return participationRequestDto;
    }
}
