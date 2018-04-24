import java.util.Vector;

/*
1. New expressions are not method calls
1.1 Do not consider constructors at all (no similarity measurement)
1.1.1 Considers classes of fields
      getMovie: {Movie} ?!
1.2.1 Considers classes where field is located
      getMovie: {Movie, Rental}

      getDaysRented: {Rental}
      with Rental: (1 / 2) / 1 = 1 / 2
      with Customer: (0 + 0) / 2 = 0
      with Movie: (1 / 2 + 1 / 2 + 1 / 2) / 3 = 1 / 2
*/
class Customer {
    private String _name;
    private Vector _rentals = new Vector();

    public Customer(String name) {
        _name = name;
    }

    public String getMovie(Movie movie) {
        Rental rental = new Rental(new Movie("", Movie.NEW_RELEASE), 10);
        Movie m = rental._movie;
        return movie.getTitle();
    }

    public void addRental(Rental arg) {
        _rentals.addElement(arg);
    }

    public String getName() {
        return _name;
    }
}