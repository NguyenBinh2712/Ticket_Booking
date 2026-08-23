package com.example.ticket.repository;

import com.example.ticket.entity.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatTypeRepository extends JpaRepository<SeatType,Long> {

}
