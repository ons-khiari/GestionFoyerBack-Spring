package com.esprit.gestionfoyerback.Service;

import com.esprit.gestionfoyerback.Entity.Bloc;
import com.esprit.gestionfoyerback.Entity.Chambre;
import com.esprit.gestionfoyerback.Entity.Etudiant;
import com.esprit.gestionfoyerback.Entity.Reservation;
import com.esprit.gestionfoyerback.Enum.TypeChambre;
import com.esprit.gestionfoyerback.Repository.ChambreRepo;
import com.esprit.gestionfoyerback.Repository.EtudiantRepo;
import com.esprit.gestionfoyerback.Repository.ReservationRepo;
import io.jsonwebtoken.lang.Assert;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReservationImp implements ReservationService {
    private final ReservationRepo reservationRepositorie;
    private final EtudiantRepo etudiantRepository;
    private final ChambreRepo chambreRepository;

    @Override
    public Reservation addReservation(Reservation reservation) {
        return reservationRepositorie.save(reservation);
    }

    @Override
    public List<Reservation> findAllReservation() {
        return (List<Reservation>)reservationRepositorie.findAll();
    }

    @Override
    public Reservation findReservationById(String id) {
        return reservationRepositorie.findById(id).isPresent() ? reservationRepositorie.findById(id).get() : null;
    }

    @Override
    public String deleteReservationById(String id) {
        if(reservationRepositorie.findById(id).isPresent()){
            reservationRepositorie.deleteById(id);
            return "Deleted"+reservationRepositorie.findById(id).get().toString();
        }else
            return "etudiant with ID : "+id+" Doesn't exist";    }

    @Override
    public Reservation updateReservation(Reservation reservation) {
        return reservationRepositorie.save(reservation);
    }

    @Override
    @Transactional
    public Reservation ajouterReservation(long idChambre, long cinEtudiant) {
        LocalDate startDate = LocalDate.of(LocalDate.now().getYear(),1,1);
        LocalDate endDate = LocalDate.of(LocalDate.now().getYear(),12,31);
        Assert.isTrue(reservationRepositorie.existsByEtudiantsCinAndAnneUniversitereBetween(cinEtudiant,startDate, endDate)," you have a reservation !!! ");
        Chambre chambre= chambreRepository.findById(idChambre).orElseThrow(() -> new IllegalArgumentException("no room found"));
        Etudiant etudiant= etudiantRepository.findByCin(cinEtudiant).orElseThrow(() -> new IllegalArgumentException("no Etudiant found"));
        String id =chambre.getNumeroChambre()+"-"+chambre.getBlocs().getNomBloc()+"-"+LocalDate.now().getYear();
        Reservation reservation= reservationRepositorie.findById(id).orElse(
                Reservation.builder()
                        .idReservation(id)
                        .anneUniversitere(LocalDate.now())
                        .estValide(true)
                        .etudiants(new ArrayList<Etudiant>()).build()
        );
        Assert.isTrue(reservation.isEstValide(),"room not available");
        reservation.getEtudiants().add(etudiant);

        if (!chambre.getReservations().contains(reservation)){
            reservation=reservationRepositorie.save(reservation);
            chambre.getReservations().add(reservation);
        }

        switch (chambre.getTypeC()){
            case SIMPLE -> reservation.setEstValide(false);
            case DOUBLE -> {if(reservation.getEtudiants().size()==2){reservation.setEstValide(false);}}
            case TRIPLE -> {if(reservation.getEtudiants().size()==3){reservation.setEstValide(false);}}
        }



        return reservation;
    }

    @Override
    @Transactional
    public Reservation annulerReservation(Long cinEtudiant) {
        // Trouver l'étudiant et sa réservation
        Etudiant etudiant = etudiantRepository.findByCin(cinEtudiant).orElseThrow(() -> new IllegalArgumentException("no etudiant"));

        // Supposition: chaque étudiant a au maximum une réservation valide
        Reservation reservation = etudiant.getReservations().stream()
                .filter(Reservation::isEstValide)
                .findFirst()
                .orElse(null);

        // Mettre à jour l'état de la réservation
        reservation.setEstValide(false);

        // Désaffecter l'étudiant
        reservation.getEtudiants().remove(etudiant);

        // Désaffecter la chambre associée et mettre à jour sa capacité
        Chambre chambreAssociee = chambreRepository.findByReservationsContains(reservation);
        if (chambreAssociee != null) {
            chambreAssociee.getReservations().remove(reservation);
            chambreRepository.save(chambreAssociee); // Sauvegarder les changements dans la chambre
        }

        // Sauvegarder les modifications
        return reservationRepositorie.save(reservation);
    }
}
