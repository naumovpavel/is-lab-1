$(document).ready(function () {
    let currentPage = 0;
    const pageSize = 10;

    function loadMoreSpaceMarines() {
        $('#load-more-space-marines-btn').prop('disabled', true);

        $.ajax({
            url: '/space-marines/json',
            type: 'GET',
            data: {
                page: currentPage + 1,
                size: pageSize
            },
            dataType: "json",
            success: function(data) {

                if (data.space-marinesList.length > 0) {
                    const select = $('#space-marines');

                    $.each(data.space-marinesList, function(index, coordinate) {
                        select.append(
                            $('<option></option>').val(coordinate.id).text("x="+coordinate.x+", y="+coordinate.y)
                        );
                    });

                    currentPage++;
                } else {
                    $('#load-more-space-marines-btn').prop('disabled', true);
                }

                $('#load-more-space-marines-btn').prop('disabled', false);
            },
            error: function(xhr, status, error) {
                alert('Error loading chapters:', error);
                $('#load-more-space-marines-btn').prop('disabled', false);
            }
        });
    }

    $('#load-more-space-marines-btn').click(loadMoreSpaceMarines);
});