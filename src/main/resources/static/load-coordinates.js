$(document).ready(function () {
    let currentPage = 0;
    const pageSize = 10;

    function loadMoreCoordinates() {
        $('#load-more-coordinates-btn').prop('disabled', true);

        $.ajax({
            url: '/coordinates/json',
            type: 'GET',
            data: {
                page: currentPage + 1,
                size: pageSize
            },
            dataType: "json",
            success: function(data) {

                if (data.coordinatesList.length > 0) {
                    const select = $('#coordinates');

                    $.each(data.coordinatesList, function(index, coordinate) {
                        select.append(
                            $('<option></option>').val(coordinate.id).text("x="+coordinate.x+", y="+coordinate.y)
                        );
                    });

                    currentPage++;
                } else {
                    $('#load-more-coordinates-btn').prop('disabled', true);
                }

                $('#load-more-coordinates-btn').prop('disabled', false);
            },
            error: function(xhr, status, error) {
                alert('Error loading chapters:', error);
                $('#load-more-coordinates-btn').prop('disabled', false);
            }
        });
    }

    $('#load-more-coordinates-btn').click(loadMoreCoordinates);
});