package com.example.ec.web;

import com.example.ec.domain.Tour;
import com.example.ec.domain.TourRating;
import com.example.ec.domain.TourRatingPk;
import com.example.ec.repo.TourRatingRepository;
import com.example.ec.repo.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Tour Rating Controller
 *
 * Created by Mary Ellen Bowman
 */
@RestController
@RequestMapping(path = "/tours/{tourId}/ratings")
public class TourRatingController {
    private TourRatingRepository tourRatingRepository;
    private TourRepository tourRepository;

    @Autowired
    public TourRatingController(TourRatingRepository tourRatingRepository, TourRepository tourRepository) {
        this.tourRatingRepository = tourRatingRepository;
        this.tourRepository = tourRepository;
    }

    protected TourRatingController() {

    }

    @PostMapping
    @ResponseStatus( HttpStatus.CREATED )
    public void createTourRating(
        @PathVariable(value = "tourId") int tourId,
        @RequestBody @Validated RatingDto ratingDto
    ) {
        Tour tour = verifyTour( tourId );
        tourRatingRepository.save(
            new TourRating(
                new TourRatingPk( tour, ratingDto.getCustomerId() ), ratingDto.getScore(), ratingDto.getComment()
            )
        );
    }

    @GetMapping
    public List<RatingDto> getAllRatingsForTour(
        @PathVariable( value = "tourId" ) int tourId
    ) {
        verifyTour( tourId );

        return tourRatingRepository.findByPkTourId( tourId ).stream().map( RatingDto::new ).collect( Collectors.toList() );
    }

    @GetMapping( path = "/average")
    public Map<String, Double> getAverage(
        @PathVariable( value = "tourId" ) int tourId
    ) {
        verifyTour( tourId );

        return Map.of(
            "average",
            tourRatingRepository.findByPkTourId( tourId ).stream()
                .mapToInt( TourRating::getScore ).average()
                .orElseThrow(
                    () -> new NoSuchElementException( "Tour has no ratings" )
                )
        );
    }

    @PutMapping
    public RatingDto updateWithPut(
        @PathVariable( value = "tourId" ) int tourId,
        @RequestBody @Validated RatingDto ratingDto
    ) {

        TourRating rating = verifyTourRating( tourId, ratingDto.getCustomerId() );
        rating.setScore( ratingDto.getScore() );
        rating.setComment( ratingDto.getComment() );

        return new RatingDto( tourRatingRepository.save( rating ) );
    }

    @PatchMapping
    public RatingDto updateWithPatch(
        @PathVariable( value = "tourId" ) int tourId,
        @RequestBody @Validated RatingDto ratingDto
    ) {
        TourRating rating = verifyTourRating( tourId, ratingDto.getCustomerId() );

        if ( ratingDto.getScore() != null ) {
            rating.setScore( ratingDto.getScore() );
        }

        if ( ratingDto.getComment() != null ) {
            rating.setComment( ratingDto.getComment() );
        }

        return new RatingDto( tourRatingRepository.save( rating ) );
    }

    @DeleteMapping( path = "/{customerId}")
    public void delete(
        @PathVariable( value = "tourId" ) int tourId,
        @PathVariable( value = "customerId" ) int customerId
    ) {
        TourRating rating = verifyTourRating( tourId, customerId );
        tourRatingRepository.delete( rating );
    }

    /**
     * Verify and return the Tour given a tourId.
     *
     * @param tourId tour identifier
     * @return the found Tour
     * @throws NoSuchElementException if no Tour found.
     */
    private Tour verifyTour(int tourId) throws NoSuchElementException {
        return tourRepository.findById(tourId).orElseThrow(() ->
            new NoSuchElementException("Tour does not exist " + tourId));
        }

    /**
     * Verify and return the TourRating for a particular tourId and Customer
     * @param tourId tour identifier
     * @param customerId customer identifier
     * @return the found TourRating
     * @throws NoSuchElementException if no TourRating found
     */
    private TourRating verifyTourRating( int tourId, int customerId ) throws NoSuchElementException {
        return tourRatingRepository
                .findByPkTourIdAndPkCustomerId( tourId, customerId )
                .orElseThrow(
                    () -> new NoSuchElementException( "Tour rating does not exist for tourId " + tourId + " and customerId " + customerId )
                    //() -> new NoSuchElementException( "Tour-Rating pair for request(" + tourId + " for customer" + customerId )
                );
    }


    /**
     * Exception handler if NoSuchElementException is thrown in this Controller
     *
     * @param ex exception
     * @return Error message String.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public String return400(NoSuchElementException ex) {
        return ex.getMessage();

    }

}
