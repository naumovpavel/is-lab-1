$(document).ready(function () {
    const socket = new SockJS('/websocket');
    const stompClient = Stomp.over(socket);
    const urlParams = new URLSearchParams(window.location.search);
    const currentPage = parseInt($('#last-page').text());
    let offset = 0;
    const pageSize = parseInt(urlParams.get('size') || 10);
    const sortBy = urlParams.get('sortBy') || 'name';
    const sortDirection = urlParams.get('sortDirection') || 'asc';
    const searchValue = urlParams.get('searchValue') || '';
    const columnName = urlParams.get('columnName') || '';

    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/chapters', function (message) {
            const data = JSON.parse(message.body);
            handleChapterEvent(data);
        });
    });

    function handleChapterEvent(data) {
        if (data.id) {
            updateOrCreateChapterRow(data);
        } else {
            offset--;
            removeChapterRow(data.deleteID);
        }
        $('#last-page').text(currentPage + Math.floor(offset / pageSize));
    }

    function updateOrCreateChapterRow(chapter) {
        let $row = $(`#${chapter.id}`);
        if ($row.length) {
            $row.remove();
        } else {
            offset++;

        }
        update(chapter);
    }

    function removeChapterRow(chapterId) {
        $(`#${chapterId}`).remove();
        const rows = $('tbody tr').get();
        if (rows.length < pageSize) {
            if(rows.length == 0 && page > 1) {
                urlParams.set('page') = page - 1;
            }
            location.reload(true);
        }
    }

    function filterRow(row) {
        if (!columnName) return true;
        return $(row).find(`td:nth-child(${getColumnIndex(columnName)})`).text().includes(searchValue);
    }


    function update(chapter) {
        const rows = $('tbody tr').get();

        const newRow = `
            <tr id="${chapter.id}">
                <td>${chapter.name}</td>
                <td>${chapter.world}</td>
                <td>${chapter.marinesCount || ''}</td>
                <td>${chapter.owner.username}</td>
                <td>
                    <a href="/chapters/edit/${chapter.id}">Редактировать</a>
                    <form action="/chapters/delete/${chapter.id}" method="post" style="display:inline;">
                        <button type="submit">Удалить</button>
                    </form>
                    <a href="/chapters/${chapter.id}">Космодесантники</a>
                </td>
            </tr>
        `;

        if (filterRow($(newRow)[0])) {
            rows.push($(newRow)[0]);
        }

        rows.sort(function (a, b) {
            let valA = $(a).find(`td:nth-child(${getColumnIndex(sortBy)})`).text();
            let valB = $(b).find(`td:nth-child(${getColumnIndex(sortBy)})`).text();

            if (getColumnIndex(sortBy) == 3) {
                valA = parseInt(valA);
                valB = parseInt(valB);
            }

            if (valA < valB) return sortDirection === 'asc' ? -1 : 1;
            if (valA > valB) return sortDirection === 'asc' ? 1 : -1;
            return 0;
        });

        if (rows.length < pageSize) {
            if(rows.length == 0 && page > 1) {
                urlParams.set('page') = page - 1;
            }
            location.reload(true);
        }

        if (rows.length > pageSize) {
            rows.pop();
        }

        $('tbody').empty();

        rows.forEach(row => {
            $('tbody').append(row);
        });

    }

    function getColumnIndex(field) {
        const columnMap = {
            name: 1,
            world: 2,
            marinesCount: 3,
            owner: 4
        };
        return columnMap[field] || 1;
    }
});