package com.reveriecraftlab.trendcanvas.dessert.controller;

import com.reveriecraftlab.trendcanvas.dessert.dto.DessertResponse;
import com.reveriecraftlab.trendcanvas.dessert.service.DessertQueryService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DessertController {

    private final DessertQueryService dessertQueryService;

    public DessertController(DessertQueryService dessertQueryService) {
        this.dessertQueryService = dessertQueryService;
    }

    /**
     * GET /api/desserts/home
     *
     * 홈에 보여줄 디저트를 무작위로 최대 5개 내려준다(FR-01). 호출할 때마다 결과가 달라진다.
     * 이미지는 여기서 내려주지 않는다. 네이버 이미지는 저장하지 않고 요청 시점에 가져와
     * 보여주기로 해서(NFR-05), 디저트마다 별도 API에서 가져온다.
     */
    @GetMapping("/api/desserts/home")
    public List<DessertResponse> getHomeDesserts() {
        return dessertQueryService.getHomeDesserts();
    }
}
