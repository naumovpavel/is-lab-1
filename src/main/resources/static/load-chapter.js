$(document).ready(function () {
    let currentPage = 0;
    const pageSize = 10;

    function loadMoreChapters() {
        $('#load-more-btn').prop('disabled', true);

        $.ajax({
            url: '/chapters/json',
            type: 'GET',
            data: {
                page: currentPage + 1,
                size: pageSize
            },
            dataType: "json",
            success: function(data) {

                if (data.chapterList.length > 0) {
                    const select = $('#chapter');

                    $.each(data.chapterList, function(index, chapter) {
                        select.append(
                            $('<option></option>').val(chapter.id).text(chapter.name)
                        );
                    });

                    currentPage++;
                } else {
                    $('#load-more-btn').prop('disabled', true);
                }

                $('#load-more-btn').prop('disabled', false);
            },
            error: function(xhr, status, error) {
                alert('Error loading chapters:', error);
                $('#load-more-btn').prop('disabled', false);
            }
        });
    }

    $('#load-more-btn').click(loadMoreChapters);
});
