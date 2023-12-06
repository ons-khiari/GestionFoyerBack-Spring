package com.esprit.gestionfoyerback.Controller;

import com.esprit.gestionfoyerback.Entity.Reservation;
import com.esprit.gestionfoyerback.Service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("reservation")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping("/getAll")
    public List<Reservation> getAllReservation() {
        return reservationService.findAllReservation();

    }

    @PostMapping("/new/{idChambre}/{cin}")
    public Reservation addReservation(@PathVariable Long idChambre, @PathVariable Long cin) {
        return reservationService.ajouterReservation(idChambre,cin);
    }


    @GetMapping("/getId/{idReservation}")
    public Reservation getId(@PathVariable String idReservation) {
        return reservationService.findReservationById(idReservation);
    }

    @DeleteMapping("/delete/{idReservation}")
    public ResponseEntity<String> deleteReservation(@PathVariable String idReservation) {
        try {
            Reservation reservation = reservationService.findReservationById(idReservation);
            if ( reservation != null) {
                reservationService.deleteReservationById(idReservation);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            // Handle other exceptions with a 500 Internal Server Error
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/annulerReservation/{cin}")
    public Reservation annulerReservation(@PathVariable Long cin) {
        return reservationService.annulerReservation(cin);
    }
}
